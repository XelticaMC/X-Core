package work.xeltica.craft.otanoshimiplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerFlagsManager {
    public PlayerFlagsManager(Plugin pl) {
        this.plugin = pl;
        PlayerFlagsManager.instance = this;
        logger = Bukkit.getLogger();
    }

    public static PlayerFlagsManager getInstance() {
        return PlayerFlagsManager.instance;
    }

    public void sync() {

    }

    public void setVisitorMode(Player p, boolean flag) {
        // 既に同値が設定されている場合はスキップ
        if (getVisitorMode(p, true) == flag) return;
        var uuid = p.getUniqueId().toString();
        if (flag) {
            // 有効化
            visitorUUIDs.add(uuid);
        } else {
            // 無効化
            visitorUUIDs.remove(uuid);
        }
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getVisitorMode(Player p) {
        return getVisitorMode(p, true);
    }

    public boolean getVisitorMode(Player p, boolean saved) {
        if (!saved && !hasOnlineStaff() && !isCitizen(p)) return true;
        return visitorUUIDs.contains(p.getUniqueId().toString());
    }

    public void reloadStore() {
        var confFile = new File(plugin.getDataFolder(), "flags.yml");
        conf = YamlConfiguration.loadConfiguration(confFile);

        visitorUUIDs = conf.getStringList("visitors");
    }

    public void writeStore() throws IOException {
        var confFile = new File(plugin.getDataFolder(), "flags.yml");
        conf.set("visitors", visitorUUIDs);
        conf.save(confFile);
        conf = YamlConfiguration.loadConfiguration(confFile);
    }

    public boolean hasOnlineStaff() {
        return _hasOnlineStaff;
    }

    public boolean isCitizen(Player p) {
        return p.hasPermission("otanoshimi.citizen");
    }

    public void updateHasOnlineStaff() {
        _hasOnlineStaff = Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.hasPermission("otanoshimi.staff"));
    }
    
    private static PlayerFlagsManager instance;
    private Plugin plugin;
    private Logger logger;
    private List<String> visitorUUIDs = new ArrayList<>();
    private boolean _hasOnlineStaff;
    private YamlConfiguration conf;
}
