package work.xeltica.craft.otanoshimiplugin.plugins;

import org.bukkit.Bukkit;

import work.xeltica.craft.otanoshimiplugin.OtanoshimiPlugin;

public abstract class PluginBase {
    public void onEnable(OtanoshimiPlugin plugin) {
        Bukkit.getLogger().info("Enabling sub-plugin " + this.getClass().getName() + "...");
    }

    public void onDisable(OtanoshimiPlugin plugin) {
        Bukkit.getLogger().info("Disabling sub-plugin " + this.getClass().getName() + "...");
    }
}
