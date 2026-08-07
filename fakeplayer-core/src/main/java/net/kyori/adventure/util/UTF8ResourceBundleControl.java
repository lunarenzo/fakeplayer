package net.kyori.adventure.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Compatibility shim for Adventure 5, which removed this utility while
 * devtools-core still uses it to load plugin translations.
 */
public final class UTF8ResourceBundleControl extends ResourceBundle.Control {

    private static final UTF8ResourceBundleControl INSTANCE = new UTF8ResourceBundleControl();

    public static ResourceBundle.Control get() {
        return INSTANCE;
    }

    public static ResourceBundle.Control utf8ResourceBundleControl() {
        return INSTANCE;
    }

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IllegalAccessException, InstantiationException, IOException {
        if (!"java.properties".equals(format)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }

        var bundleName = toBundleName(baseName, locale);
        var resourceName = toResourceName(bundleName, "properties");
        InputStream stream;
        if (reload) {
            URL resource = loader.getResource(resourceName);
            URLConnection connection = resource == null ? null : resource.openConnection();
            if (connection == null) {
                return null;
            }
            connection.setUseCaches(false);
            stream = connection.getInputStream();
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream == null) {
            return null;
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
}
