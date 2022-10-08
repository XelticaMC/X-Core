package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.utils.Config;

/**
 * ワールドを管理するストアです。
 * @author Xeltica
 */
public class WorldStore {
    public WorldStore() {
        WorldStore.instance = this;
        loadWorldName();
        loadWorldDescription();
        loadLockedWorldNames();
        loadCreativeWorldNames();
        location = new Config("location");
    }

    public static WorldStore getInstance() {
        return instance;
    }

    public String getWorldDisplayName(World w) {
        return getWorldDisplayName(w.getName());
    }

    public String getWorldDisplayName(String n) {
        return worldNameMap.get(n);
    }

    public String getWorldDescription(World w) {
        return getWorldDescription(w.getName());
    }

    public String getWorldDescription(String n) {
        return worldDescMap.get(n);
    }

    public boolean isCreativeWorld(World w) {
        return isCreativeWorld(w.getName());
    }

    public boolean isCreativeWorld(String n) {
        return creativeWorldNames.contains(n);
    }

    public boolean isLockedWorld(World w) {
        return isLockedWorld(w.getName());
    }

    public boolean isLockedWorld(String n) {
        return lockedWorldNames.contains(n);
    }

    public boolean canSummonVehicles(World w) {
        return canSummonVehicles(w.getName());
    }

    public boolean canSummonVehicles(String worldName) {
        return List.of(summonVehicleWhiteList).contains(worldName);
    }

    public void saveCurrentLocation(Player p) {
        final var conf = location.getConf();
        final var pid = p.getUniqueId().toString();
        var playerSection = conf.getConfigurationSection(pid);
        if (playerSection == null) {
            playerSection = conf.createSection(pid);
        }
        playerSection.set(p.getWorld().getName(), p.getLocation());
        try {
            location.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location getLocation(Player p, String name) {
        final var conf = location.getConf();
        final var pid = p.getUniqueId().toString();
        final var playerSection = conf.getConfigurationSection(pid);
        if (playerSection == null) {
            return null;
        }
        return playerSection.getLocation(name);
    }

    public void teleport(Player player, String worldName) {
        final var world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§bテレポートに失敗しました。ワールドが存在しないようです。");
            return;
        }
        player.teleportAsync(world.getSpawnLocation());
    }

    public void teleportToSavedLocation(Player player, String worldName) {
        if (player.getWorld().getName().equals(worldName)) {
            Gui.getInstance().error(player, "既に" + WorldStore.getInstance().getWorldDisplayName(worldName) + "にいます。");
            return;
        }
        final var loc = getLocation(player, worldName);
        if (loc == null) {
            // 保存されていなければ普通にTP
            teleport(player, worldName);
            return;
        }
        player.teleportAsync(loc);
    }

    public void deleteSavedLocation(String worldName) {
        final var conf = location.getConf();
        conf.getKeys(false).forEach(pid -> {
            var playerSection = conf.getConfigurationSection(pid);
            if (playerSection == null) {
                playerSection = conf.createSection(pid);
            }
            playerSection.set(worldName, null);
        });
        try {
            location.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteSavedLocation(String worldName, Player player) {
        final var conf = location.getConf();
        final var pid = player.getUniqueId().toString();
        var playerSection = conf.getConfigurationSection(pid);
        if (playerSection == null) {
            playerSection = conf.createSection(pid);
        }
        playerSection.set(worldName, null);
        try {
            location.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public String getRespawnWorld(World w) {
        return getRespawnWorld(w.getName());
    }

    public String getRespawnWorld(String worldName) {
        return switch (worldName) {
            case "wildarea2_nether", "wildarea2_the_end" -> "wildarea2";
            case "pvp", "wildareab", "hub2" -> null;
            default -> worldName;
        };
    }

    private void loadWorldName() {
        worldNameMap.put("main", "メインワールド");
        worldNameMap.put("sandbox2", "サンドボックス");
        worldNameMap.put("art", "アートワールド");
        worldNameMap.put("nightmare2", "ナイトメア");
        worldNameMap.put("pvp", "PvPアリーナ");
        worldNameMap.put("test", "実験ワールド");
        worldNameMap.put("wildarea2", "共有ワールド");
        worldNameMap.put("wildarea2_nether", "共有ネザー");
        worldNameMap.put("wildarea2_the_end", "共有エンド");
        worldNameMap.put("wildareab", "資源ワールド");
        worldNameMap.put("shigen_nether", "資源ネザー");
        worldNameMap.put("shigen_end", "資源エンド");
        worldNameMap.put("hub2", "ロビー");
        worldNameMap.put("event", "イベントワールド");
        worldNameMap.put("event2", "イベントワールド");
    }

    private void loadWorldDescription() {
        worldDescMap.put("sandbox2",
            "ここは、§bクリエイティブモード§rで好きなだけ遊べる§cサンドボックスワールド§r。\n" +
            "元の世界の道具や経験値はお預かりしているので、好きなだけあそんでね！" +
            "§7(あ、でも他の人の建築物を壊したりしないでね)"
        );

        worldDescMap.put("nightmare2",
            "ここは怖い敵がうじゃうじゃいる§cナイトメアワールド§r。\n" +
            "手に入れたアイテムは持ち帰れます。"
        );

        worldDescMap.put("art",
            "ここは、§b地上絵§rに特化した§cアートワールド§r。\n" +
            "元の世界の道具や経験値はお預かりしているので、安心して地上絵を作成・観覧できます！\n" +
            "§7(他の人の作った地上絵を壊さないようお願いします。)"
        );

        worldDescMap.put("wildarea2",
            "ここは、§c共有ワールド§r。\n" +
            "誰かが寄付してくれた資源を共有拠点にしまってあるので、有効にご活用ください。（独り占めはダメです）"
        );

        worldDescMap.put("wildareab",
            "ここは、§c資源ワールド§r。\n" +
            "メインワールドで生活するための資源を探そう。"
        );
    }

    private void loadLockedWorldNames() {
        lockedWorldNames.add("sandbox2");
        lockedWorldNames.add("art");
        lockedWorldNames.add("nightmare2");
    }

    private void loadCreativeWorldNames() {
        creativeWorldNames.add("art");
        creativeWorldNames.add("sandbox2");
        creativeWorldNames.add("test");
    }

    private static WorldStore instance;
    private final Config location;
    private final Map<String, String> worldNameMap = new HashMap<>();
    private final Map<String, String> worldDescMap = new HashMap<>();
    private final Set<String> lockedWorldNames = new HashSet<>();
    private final Set<String> creativeWorldNames = new HashSet<>();

    private final String[] summonVehicleWhiteList = {
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "main",
        "nightmare2",
        "wildareab",
    };
}
