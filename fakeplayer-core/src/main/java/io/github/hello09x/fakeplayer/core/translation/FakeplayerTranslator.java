package io.github.hello09x.fakeplayer.core.translation;

import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public final class FakeplayerTranslator implements Translator {

    private final Plugin plugin;
    private final String baseName;
    private final Locale defaultLocale;
    private final ClassLoader[] classLoaders;
    private final Set<Locale> loadedLocales = new HashSet<>();
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
    public @Nullable Component translate(TranslatableComponent component, Locale locale) {
        return null;
    }

    public synchronized void reload() {
        ResourceBundle.clearCache();
        loadedLocales.clear();
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
                    bundle = ResourceBundle.getBundle(
                            baseName,
                            locale,
                            classLoader,
                            UTF8ResourceBundleControl.get()
                    );
                } catch (MissingResourceException ignored) {
                    continue;
                }

                if (loadedLocales.contains(bundle.getLocale())) {
                    break;
                }
                store.registerAll(locale, bundle, false);
                break;
            }
        } finally {
            loadedLocales.add(locale);
        }
    }
}
