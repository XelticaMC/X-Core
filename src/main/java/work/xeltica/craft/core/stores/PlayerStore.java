package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.events.StaffJoinEvent;
import work.xeltica.craft.core.events.StaffLeaveEvent;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.models.PlayerRecord;
import work.xeltica.craft.core.utils.Config;

public class PlayerStore {
    public PlayerStore() {
        PlayerStore.instance = this;
        flags = new Config("flags");
        newcomers = new Config("newcomers");
        playerStores = new Config("playerStores");

        checkAndMigrate();
    }

    public static PlayerStore getInstance() {
        return PlayerStore.instance;
    }

    public PlayerRecord open(OfflinePlayer p) {
        return open(p.getUniqueId());
    }

    public PlayerRecord open(UUID id) {
        var section = playerStores.getConf().getConfigurationSection(id.toString());
        if (section == null) {
            section = playerStores.getConf().createSection(id.toString());
        }
        return new PlayerRecord(playerStores, section, id);
    }

    public List<PlayerRecord> openAll() {
        return playerStores
            .getConf()
            .getKeys(false)
            .stream()
            .map(k -> open(UUID.fromString(k)))
            .toList();
    }

    public void save() throws IOException {
        playerStores.save();
    }

    public boolean hasOnlineStaff() {
        return _hasOnlineStaff;
    }

    public boolean isCitizen(Player p) {
        return p.hasPermission("otanoshimi.citizen");
    }

    public void updateHasOnlineStaff() {
        var flag = Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.hasPermission("otanoshimi.staff"));
        if (_hasOnlineStaff != flag) {
            Bukkit.getPluginManager().callEvent(flag ? new StaffJoinEvent() : new StaffLeaveEvent());
        }
        _hasOnlineStaff = flag;
    }

    public void setLiveMode(Player player, boolean isLive) {
        if (isLive == isLiveMode(player)) return;
        if (isLive) {
            var name = String.format("%s が配信中", player.getName());
            var bar = BossBar.bossBar(Component.text(name), BossBar.MAX_PROGRESS, Color.RED, Overlay.PROGRESS);

            liveBarMap.put(player.getUniqueId(), bar);
            BossBarStore.getInstance().add(bar);
        } else {                
            var bar = liveBarMap.get(player.getUniqueId());

            liveBarMap.remove(player.getUniqueId());
            BossBarStore.getInstance().remove(bar);
        }
    }

    public boolean isLiveMode(Player p) {
        return liveBarMap.containsKey(p.getUniqueId());
    }

    private void checkAndMigrate() {
        if (!Config.exists("flags") && !Config.exists("newcomers")) return;

        Bukkit.getLogger().info("古い保存データが残っています。Player Storeへのマイグレーションを行います。");
        try {
            migrate();
            Config.delete("flags");
            Config.delete("newcomers");
            Bukkit.getLogger().info("マイグレーションが完了しました。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void migrate() throws IOException {
        // cat
        var catUUIDs = flags.getConf().getStringList("cats").stream().map(id -> UUID.fromString(id)).toList();
        for (var id : catUUIDs) {
            open(id).set(PlayerDataKey.CAT_MODE, true, false);
        }

        // new comers
        var newComerIds = newcomers.getConf().getKeys(false);
        for (var id : newComerIds) {
            open(UUID.fromString(id)).set(PlayerDataKey.NEWCOMER_TIME, newcomers.getConf().getInt(id), false);
        }

        playerStores.save();
    }
    
    private static PlayerStore instance;
    private boolean _hasOnlineStaff;
    private Config flags;
    private Config newcomers;
    private Config playerStores;
    private Map<UUID, BossBar> liveBarMap = new HashMap<>();
}