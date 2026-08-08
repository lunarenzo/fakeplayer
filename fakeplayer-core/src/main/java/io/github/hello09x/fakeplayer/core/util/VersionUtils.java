package io.github.hello09x.fakeplayer.core.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class VersionUtils {

    private VersionUtils() {
    }

    public static @NotNull String getMinecraftVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    public static @NotNull String getMinecraftMajorMinor() {
        var parts = getMinecraftVersion().split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return getMinecraftVersion();
    }
    public static boolean isAtLeast(int major, int minor, int patch) {
        var parts = getMinecraftVersion().split("\\.");
        var expected = new int[]{major, minor, patch};
        for (var i = 0; i < expected.length; i++) {
            var current = i < parts.length ? Integer.parseInt(parts[i]) : 0;
            if (current != expected[i]) {
                return current > expected[i];
            }
        }
        return true;
    }


    public static boolean isSupported(@NotNull Set<String> supports) {
        return supports.contains(getMinecraftVersion());
    }

    public record MinecraftVersion(int major, int minor, int patch) implements Comparable<MinecraftVersion> {
        public static @NotNull MinecraftVersion parse(@NotNull String version) {
            var parts = version.split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new MinecraftVersion(major, minor, patch);
        }

        @Override
        public int compareTo(@NotNull MinecraftVersion o) {
            if (this.major != o.major) {
                return Integer.compare(this.major, o.major);
            }
            if (this.minor != o.minor) {
                return Integer.compare(this.minor, o.minor);
            }
            return Integer.compare(this.patch, o.patch);
        }
    }

    public static @NotNull MinecraftVersion getBridgeVersion(@NotNull io.github.hello09x.fakeplayer.api.spi.NMSBridge bridge) {
        String name = bridge.getClass().getPackageName();
        var parts = name.split("\\.");
        for (var part : parts) {
            if (part.startsWith("v")) {
                var verStr = part.substring(1).replace('_', '.');
                return MinecraftVersion.parse(verStr);
            }
        }
        throw new IllegalArgumentException("Unknown NMSBridge package: " + name);
    }
}
