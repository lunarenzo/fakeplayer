package io.github.hello09x.fakeplayer.core.translation;

import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public final class FakeplayerTranslator implements Translator {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Plugin plugin;
    private final String baseName;
    private final Locale defaultLocale;
    private final ClassLoader[] classLoaders;
    private final Set<Locale> loadedLocales = new HashSet<>();
    private final Map<Locale, Map<String, String>> templates = new HashMap<>();
    private TranslationStore.StringBased<MessageFormat> store;

    public FakeplayerTranslator(Plugin plugin, String baseName, Locale defaultLocale) {
        this.plugin = plugin;
        this.baseName = baseName;
        this.defaultLocale = defaultLocale;
        this.classLoaders = new ClassLoader[]{
                TranslatorUtils.getDataFolderClassLoader(plugin),
                TranslatorUtils.getJarClassLoader(plugin)
        };
        this.store = createStore();
    }

    @Override
    public Key name() {
        return store.name();
    }

    @Override
    public synchronized @Nullable MessageFormat translate(String key, Locale locale) {
        var actualLocale = locale == null ? defaultLocale : locale;
        loadLocaleLazily(actualLocale);
        return store.translate(key, actualLocale);
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized @Nullable Component translate(TranslatableComponent component, Locale locale) {
        var actualLocale = locale == null ? defaultLocale : locale;
        loadLocaleLazily(actualLocale);

        var localizedTemplates = templates.get(actualLocale);
        if (localizedTemplates == null) {
            return null;
        }

        var template = localizedTemplates.get(component.key());
        if (template == null) {
            return null;
        }

        var arguments = component.args();
        var resolver = TagResolver.builder();
        for (var i = 0; i < arguments.size(); i++) {
            var name = "arg" + i;
            template = template.replace("{" + i + "}", "<" + name + ">");
            resolver.resolver(Placeholder.component(name, arguments.get(i)));
        }

        return Component.text()
                .style(component.style())
                .append(MINI_MESSAGE.deserialize(template, resolver.build()))
                .append(component.children())
                .build();
    }

    public synchronized void reload() {
        ResourceBundle.clearCache();
        loadedLocales.clear();
        templates.clear();
        store = createStore();
    }

    private TranslationStore.StringBased<MessageFormat> createStore() {
        var result = TranslationStore.messageFormat(Key.key(plugin.getName().toLowerCase(Locale.ROOT)));
        result.defaultLocale(defaultLocale);
        return result;
    }

    private void loadLocaleLazily(Locale locale) {
        if (loadedLocales.contains(locale)) {
            return;
        }

        try {
            for (var classLoader : classLoaders) {
                ResourceBundle bundle;
                try {
                    bundle = ResourceBundle.getBundle(baseName, locale, classLoader);
                } catch (MissingResourceException ignored) {
                    continue;
                }

                if (loadedLocales.contains(bundle.getLocale())) {
                    break;
                }
                var localizedTemplates = new HashMap<String, String>();
                var formats = new HashMap<String, MessageFormat>();
                for (var key : bundle.keySet()) {
                    var template = bundle.getString(key);
                    localizedTemplates.put(key, template);
                    formats.put(key, new MessageFormat(MINI_MESSAGE.stripTags(template), locale));
                }
                templates.put(locale, localizedTemplates);
                store.registerAll(locale, formats);
                break;
            }
        } finally {
            loadedLocales.add(locale);
        }
    }

}
