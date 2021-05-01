package work.xeltica.craft.otanoshimiplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import work.xeltica.craft.otanoshimiplugin.events.StaffJoinEvent;
import work.xeltica.craft.otanoshimiplugin.events.StaffLeaveEvent;

public class PlayerFlagsManager {
    public PlayerFlagsManager(Plugin pl) {
        this.plugin = pl;
        PlayerFlagsManager.instance = this;
        logger = Bukkit.getLogger();
        reloadStore();
    }

    public static PlayerFlagsManager getInstance() {
        return PlayerFlagsManager.instance;
    }

    public void tickNewcomers(int tick) {
        newcomersConf.getKeys(false).forEach(key -> {
            if (Bukkit.getPlayer(UUID.fromString(key)) == null) return;
            var time = newcomersConf.getInt(key, 0);
            time -= tick;
            newcomersConf.set(key, time <= 0 ? null : time);
        });
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewcomer(Player p) {
        newcomersConf.set(p.getUniqueId().toString(), 20 * 60 * 30);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNewcomer(Player p) {
        return newcomersConf.contains(p.getUniqueId().toString());
    }

    public int getNewcomerTime(Player p) {
        return newcomersConf.getInt(p.getUniqueId().toString(), 0) / 20;
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
        return getVisitorMode(p, false);
    }

    public boolean getVisitorMode(Player p, boolean saved) {
        if (!saved && !hasOnlineStaff() && !isCitizen(p)) return true;
        return visitorUUIDs.contains(p.getUniqueId().toString());
    }

    public void reloadStore() {
        var flagsConfFile = new File(plugin.getDataFolder(), "flags.yml");
        flagsConf = YamlConfiguration.loadConfiguration(flagsConfFile);

        visitorUUIDs = flagsConf.getStringList("visitors");

        var newcomersConfFile = new File(plugin.getDataFolder(), "newcomers.yml");
        newcomersConf = YamlConfiguration.loadConfiguration(newcomersConfFile);
    }

    public void writeStore() throws IOException {
        var flagsConfFile = new File(plugin.getDataFolder(), "flags.yml");
        flagsConf.set("visitors", visitorUUIDs);
        flagsConf.save(flagsConfFile);
        flagsConf = YamlConfiguration.loadConfiguration(flagsConfFile);
        var newcomersConfFile = new File(plugin.getDataFolder(), "newcomers.yml");
        newcomersConf.save(newcomersConfFile);
        newcomersConf = YamlConfiguration.loadConfiguration(newcomersConfFile);
    }

    public boolean hasOnlineStaff() {
        return _hasOnlineStaff;
    }

    public boolean isCitizen(Player p) {
        return p.hasPermission("otanoshimi.citizen");
    }

    public void updateHasOnlineStaff() {
        if (_hasOnlineStaff != flag) {
            Bukkit.getPluginManager().callEvent(flag ? new StaffJoinEvent() : new StaffLeaveEvent());
        }
        _hasOnlineStaff = flag;
    }
    
    private static PlayerFlagsManager instance;
    private Plugin plugin;
    private Logger logger;
    private List<String> visitorUUIDs = new ArrayList<>();
    private boolean _hasOnlineStaff;
    private YamlConfiguration flagsConf;
    private YamlConfiguration newcomersConf;
}
