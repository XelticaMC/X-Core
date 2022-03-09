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
        return n.startsWith("travel_") ? null : worldNameMap.get(n);
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
        final var loc = getLocation(player, worldName);
        if (loc == null) {
            // 保存されていなければ普通にTP
            teleport(player, worldName);
            return;
        }
        player.teleportAsync(loc);
    }

    @Nullable
    public String getRespawnWorld(World w) {
        return getRespawnWorld(w.getName());
    }

    public String getRespawnWorld(String worldName) {
        // TODO 旅行券のときに位置情報保存しておいてーとかそういう処理に対応したい
        if (worldName.startsWith("travel_")) return "world";
        return switch (worldName) {
            case "wildarea2_nether", "wildarea2_the_end" -> "wildarea2";
            case "world_nether", "nightmare", "wildarea", "world_the_end" -> "world";
            case "pvp", "wildareab", "hub", "hub2" -> null;
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
        worldNameMap.put("wildarea2", "ワイルドエリアA");
        worldNameMap.put("wildareab", "ワイルドエリアB");
        worldNameMap.put("wildarea2_nether", "ワイルドネザー");
        worldNameMap.put("wildarea2_the_end", "ワイルドエンド");
        worldNameMap.put("hub2", "ロビー");

        worldNameMap.put("world", "クラシックワールド");
        worldNameMap.put("world_nether", "クラシックネザー");
        worldNameMap.put("world_the_end", "クラシックエンド");
        worldNameMap.put("hub", "クラシックロビー");
        worldNameMap.put("sandbox", "サンドボックス(クラシック)");
        worldNameMap.put("nightmare", "ナイトメア(クラシック)");
        worldNameMap.put("wildarea", "ワイルドエリア(クラシック)");
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
        worldDescMap.put("test",
            "よくここを見つけたな...。ここはデバッグワールド。\n" +
            "XelticaMC の開発者が機能をテストするために開放している場所。\n" +
            "自由に立ち入りできますが、何が起こってもサポートは致しかねます。"
        );

        worldDescMap.put("wildarea",
            "ここは、資源が豊富な§cワイルドエリア§r。\n" +
            "メインワールドで生活するための資源を回収したり、サバイバル生活をしたり、使い方は無限大。"
        );

        worldDescMap.put("wildarea2",
            "ここは、資源が豊富な§cワイルドエリアA§r。\n" +
            "メインワールドで生活するための資源を回収したり、サバイバル生活をしたり、使い方は無限大。"
        );

        worldDescMap.put("wildareab",
                "ここは、資源が豊富な§cワイルドエリアB§r。\n" +
                "メインワールドで生活するための資源を回収したり、サバイバル生活をしたり、使い方は無限大。"
        );
    }

    private void loadLockedWorldNames() {
        lockedWorldNames.add("art");
        lockedWorldNames.add("sandbox");
        lockedWorldNames.add("nightmare");
        lockedWorldNames.add("nightmare2");
    }

    private void loadCreativeWorldNames() {
        creativeWorldNames.add("art");
        creativeWorldNames.add("test");
        creativeWorldNames.add("sandbox");
    }

    private static WorldStore instance;
    private final Config location;
    private final Map<String, String> worldNameMap = new HashMap<>();
    private final Map<String, String> worldDescMap = new HashMap<>();
    private final Set<String> lockedWorldNames = new HashSet<>();
    private final Set<String> creativeWorldNames = new HashSet<>();

    private final String[] summonVehicleWhiteList = {
        "world",
        "world_nether",
        "world_the_end",
        "wildarea",
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "main",
        "nightmare2",
    };
}
