package io.github.hello09x.fakeplayer.core.translation;

import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
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

        var arguments = translationArguments(component);
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
    @SuppressWarnings("unchecked")
    private static @Nullable List<Component> legacyArguments(TranslatableComponent component) {
        if (LegacyArguments.GET_ARGUMENTS == null) {
            return null;
        }
        try {
            return (List<Component>) LegacyArguments.GET_ARGUMENTS.invokeExact(component);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to read legacy translation arguments", e);
        }
    }

    private static List<Component> translationArguments(TranslatableComponent component) {
        var legacyArguments = legacyArguments(component);
        if (legacyArguments != null) {
            return legacyArguments;
        }
        return ModernArguments.get(component);
    }

    private static final class LegacyArguments {

        private static final MethodHandle GET_ARGUMENTS = findGetter();

        private static @Nullable MethodHandle findGetter() {
            try {
                return MethodHandles.publicLookup().findVirtual(
                        TranslatableComponent.class,
                        "args",
                        MethodType.methodType(List.class)
                );
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
                return null;
            }
        }
    }

    private static final class ModernArguments {

        private static List<Component> get(TranslatableComponent component) {
            return component.arguments().stream()
                    .map(argument -> argument.value() instanceof ComponentLike componentLike
                            ? componentLike.asComponent()
                            : Component.text(String.valueOf(argument.value())))
                    .toList();
        }
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
