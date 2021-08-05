package work.xeltica.craft.core.utils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.bukkit.configuration.file.YamlConfiguration;

import work.xeltica.craft.core.XCorePlugin;

/**
 * Spigot の設定ファイル機能を扱いやすいように、面倒な部分をラッピングしています。
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class Config {
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
