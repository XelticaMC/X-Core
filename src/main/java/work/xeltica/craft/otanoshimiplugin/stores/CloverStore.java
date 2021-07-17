package work.xeltica.craft.otanoshimiplugin.stores;

import java.io.File;
import java.io.IOException;

import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import work.xeltica.craft.otanoshimiplugin.plugins.VaultPlugin;

public class CloverStore {
    public CloverStore(final Plugin pl) {
        CloverStore.instance = this;
        cloversConfFile = new File(pl.getDataFolder(), "clovers.yml");
        loadCloversConf();
    }

    public static CloverStore getInstance() {
        return instance;
    }

    public void loadCloversConf() {
        cloversConf = YamlConfiguration.loadConfiguration(cloversConfFile);
    }

    public void saveCloversConf() {
        try {
            cloversConf.save(cloversConfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadCloversConf();
    }

    public void saveAllCloversAccount() {
        final var eco = plugin().getEconomy();
        final var players = Lists.newArrayList(Bukkit.getOfflinePlayers());
        final var logger = Bukkit.getLogger();
        players.addAll(Bukkit.getOnlinePlayers());
        for (var p : players) {
            final var balance = eco.getBalance(p);
            if (balance == 0) continue;
            cloversConf.set(p.getUniqueId().toString(), balance);
            logger.info(String.format("%sさんの残高 %f Clover をデポジット", p.getName(), balance));
        }
        saveCloversConf();
    }

    private VaultPlugin plugin() {
        return VaultPlugin.getInstance();
    }

    private static CloverStore instance;
    private YamlConfiguration cloversConf;
    private File cloversConfFile;
}
