package work.xeltica.craft.core.stores;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.events.StaffJoinEvent;
import work.xeltica.craft.core.events.StaffLeaveEvent;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.models.PlayerRecord;
import work.xeltica.craft.core.utils.Config;
import work.xeltica.craft.core.utils.Ticks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * プレイヤー固有データの管理を行い、読み書きを行うPlayer Store APIを提供します。
 * @author Xeltica
 */
public class PlayerStore {
    public PlayerStore() {
        PlayerStore.instance = this;
        PlayerStore.liveBarMap = new HashMap<>();
        playerStores = new Config("playerStores");

        Bukkit.getScheduler().runTaskTimer(XCorePlugin.getInstance(), this::saveTask, 0, Ticks.from(10));
    }

    public static PlayerStore getInstance() {
        return PlayerStore.instance;
    }

    public static Map<UUID, BossBar> getliveBarMap() {
        return PlayerStore.liveBarMap;
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

    public boolean isCitizen(Player p) {
        return p.hasPermission("otanoshimi.citizen");
    }

    public void setLiveMode(Player player, boolean isLive) {
        if (isLive == isLiveMode(player)) return;
        if (isLive) {
            final var name = String.format("%s が配信中", player.getName());
            final var bar = BossBar.bossBar(Component.text(name), BossBar.MAX_PROGRESS, Color.RED, Overlay.PROGRESS);

            liveBarMap.put(player.getUniqueId(), bar);
            BossBarStore.getInstance().add(bar);
        } else {
            final var bar = liveBarMap.get(player.getUniqueId());

            liveBarMap.remove(player.getUniqueId());
            BossBarStore.getInstance().remove(bar);
        }
    }

    public boolean isLiveMode(Player p) {
        return liveBarMap.containsKey(p.getUniqueId());
    }

    public ItemStack getRandomFireworkByUUID(UUID id, int amount) {
        final var random = new Random(id.hashCode());
        final var item = new ItemStack(Material.FIREWORK_ROCKET, amount);
        item.editMeta(meta -> {
            final var firework = (FireworkMeta) meta;
            final var effect = FireworkEffect.builder()
                    .trail(random.nextBoolean())
                    .flicker(random.nextBoolean())
                    .with(FireworkEffect.Type.values()[random.nextInt(5)])
                    .withColor(colors[random.nextInt(colors.length)])
                    .build();
            firework.addEffect(effect);
            firework.setPower(1);
        });
        return item;
    }

    public boolean isOnline(Player player) {
        return isOnline(player.getUniqueId());
    }

    public boolean isOnline(UUID id) {
        return Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.getUniqueId().equals(id));
    }

    private void saveTask() {
        if (!isChanged()) return;
        try {
            save();
            setChanged(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PlayerStore instance;
    private final Config playerStores;
    private static Map<UUID, BossBar> liveBarMap;

    private boolean changed;

    private static final org.bukkit.Color[] colors = {
            // from XelticaUI
            org.bukkit.Color.fromRGB(0xe23731), // red
            org.bukkit.Color.fromRGB(0xeb6101), // vermilion
            org.bukkit.Color.fromRGB(0xf08300), // orange
            org.bukkit.Color.fromRGB(0xe9be00), // yellow
            org.bukkit.Color.fromRGB(0xb8d200), // lime
            org.bukkit.Color.fromRGB(0x3eb370), // green
            org.bukkit.Color.fromRGB(0x20c0a0), // teal
            org.bukkit.Color.fromRGB(0x43fcf3), // cyan
            org.bukkit.Color.fromRGB(0x00b7ff), // skyblue
            org.bukkit.Color.fromRGB(0x2571ff), // blue
            org.bukkit.Color.fromRGB(0xff55a1), // magenta
            org.bukkit.Color.fromRGB(0xff5c84), // pink
    };

    public boolean isChanged() {
        return this.changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
