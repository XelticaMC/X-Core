package work.xeltica.craft.core.api;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import work.xeltica.craft.core.XCorePlugin;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 時間をMinecraft Tickに変換するのと、その逆をする機能を持つ
 * @author Xeltica
 */
public class Ticks {
    /** 秒をTickに変換 */
    public static int from(double second) {
        return (int)(second * 20);
    }

    /** 分、秒をTickに変換 */
    public static int from(int minute, double second) {
        return from(minute * 60) + from(second);
    }

    /** 時間、分、秒をTickに変換 */
    public static int from(int hour, int minute, double second) {
        return from(hour * 3600) + from(minute * 60) + from(second);
    }

    /** Tickを秒に変換 */
    public static double toTime(int tick) {
        return tick / 20.0;
    }

    /**
     * 2つのLocationが一致しているかどうかを計算します。
     * @author Xeltica
     */
    public static class LocationComparator {
        public static boolean equals(Location l1, Location l2) {
            if (!l1.getWorld().equals(l2.getWorld())) return false;

            final var x1 = l1.getX();
            final var y1 = l1.getY();
            final var z1 = l1.getZ();

            final var x2 = l2.getX();
            final var y2 = l2.getY();
            final var z2 = l2.getZ();
            return x1 == x2 && y1 == y2 && z1 == z2;
        }
    }

    public static class Time {
        public static String msToString(long timeInMilliseconds) {
            final var _ms = timeInMilliseconds;
            final var ms = _ms % 1000;
            final var _s = _ms / 1000;
            final var s = _s % 60;
            final var _m = _s / 60;
            final var m = _m % 60;
            final var _h = _m / 60;
            final var h = _h % 60;
            return h == 0
                ? String.format("%02d:%02d.%03d", m, s, ms)
                : String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
        }
    }

    /**
     * Spigot の設定ファイル機能を扱いやすいように、面倒な部分をラッピングしています。
     * @author Xeltica
     */
    @SuppressWarnings("ALL")
    public static class Config {
        public Config(String configName) {
            this(configName, null);
        }

        public Config(String configName, Consumer<Config> onReloaded) {
            this.configName = configName;
            this.onReloaded = onReloaded;
            this.reload();
        }

        public String getConfigName() {
            return this.configName;
        }

        public YamlConfiguration getConf() {
            return this.config;
        }

        public void reload() {
            this.config = YamlConfiguration.loadConfiguration(this.openFile());
            if (onReloaded != null) onReloaded.accept(this);
        }

        public void save() throws IOException {
            this.config.save(this.openFile());
            this.reload();
        }

        public static boolean exists(String configName) {
            return openFile(configName).exists();
        }

        public static boolean delete(String configName) {
            return openFile(configName).delete();
        }

        private File openFile() {
            return openFile(this.configName);
        }

        private static File openFile(String configName) {
            final var folder = XCorePlugin.getInstance().getDataFolder();
            return new File(folder, configName + ".yml");
        }

        private final String configName;
        private final Consumer<Config> onReloaded;
        private YamlConfiguration config;
    }
}
