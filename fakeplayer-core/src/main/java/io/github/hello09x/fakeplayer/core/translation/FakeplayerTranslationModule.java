package io.github.hello09x.fakeplayer.core.translation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public final class FakeplayerTranslationModule extends AbstractModule {

    private final String baseName;
    private final Locale defaultLocale;

    public FakeplayerTranslationModule(String baseName, Locale defaultLocale) {
        this.baseName = baseName;
        this.defaultLocale = defaultLocale;
    }

    @Provides
    @Singleton
    public FakeplayerTranslator fakeplayerTranslator(Plugin plugin) {
        var translator = new FakeplayerTranslator(plugin, baseName, defaultLocale);
        GlobalTranslator.translator().addSource(translator);
        return translator;
    }
}
