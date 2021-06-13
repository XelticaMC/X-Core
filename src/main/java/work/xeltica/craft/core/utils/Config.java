package work.xeltica.craft.core.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import work.xeltica.craft.core.XCorePlugin;

public class Config {
    public Config(String configName) {
        this.configName = configName;
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
    }

    public void save() throws IOException {
        this.config.save(this.openFile());
        this.reload();
    }

    private File openFile() {
        var folder = XCorePlugin.getInstance().getDataFolder();
        return new File(folder, this.configName + ".yml");
    }

    private final String configName;
    private YamlConfiguration config;
}
