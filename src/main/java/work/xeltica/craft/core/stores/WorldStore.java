package work.xeltica.craft.core.stores;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldStore {
    public WorldStore() {
        WorldStore.instance = this;
        loadWorldName();
        loadWorldDescription();
        loadLockedWorldNames();
        loadCreativeWorldNames();
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

    public void teleport(Player player, String worldName) {
        var world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§bテレポートに失敗しました。ワールドが存在しないようです。");
            return;
        }
        player.teleport(world.getSpawnLocation());
    }

    private void loadWorldName() {
        worldNameMap.put("world", "メインワールド");
        worldNameMap.put("world_nether", "ネザー");
        worldNameMap.put("world_the_end", "ジ・エンド");
        worldNameMap.put("hub", "ロビー");
        worldNameMap.put("sandbox", "サンドボックス");
        worldNameMap.put("nightmare", "ナイトメア");
        worldNameMap.put("art", "アートワールド");
        worldNameMap.put("pvp", "PVPワールド");
        worldNameMap.put("test", "テストワールド");
    }

    private void loadWorldDescription() {
        worldDescMap.put("sandbox", 
            "ここは、§bクリエイティブモード§rで好きなだけ遊べる§cサンドボックスワールド§r。\n" +
            "元の世界の道具や経験値はお預かりしているので、好きなだけあそんでね！" +
            "§7(あ、でも他の人の建築物を壊したりしないでね)" +
            "帰るときは、§a/hub §rコマンドを実行してください。"
        );
        worldDescMap.put("nightmare",
            "ここは怖い敵がうじゃうじゃいる§cナイトメアワールド§r。\n" +
            "手に入れたアイテムは持ち帰れます。\n" +
            "帰るときは、§a/hub §rコマンドを実行してください。"
        );
        worldDescMap.put("art",
            "ここは、§b地上絵§rに特化した§cアートワールド§r。\n" +
            "元の世界の道具や経験値はお預かりしているので、安心して地上絵を作成・観覧できます！\n" +
            "§7(他の人の作った地上絵を壊さないようお願いします。)\n" +
            "帰るときは、§a/hub §rコマンドを実行してください。"
        );
        worldDescMap.put("test",
            "よくここを見つけたな...。ここはデバッグワールド。\n" +
            "XelticaMC の開発者が機能をテストするために開放している場所。\n" + 
            "自由に立ち入りできますが、何が起こってもサポートは致しかねます。"
        );
    }

    private void loadLockedWorldNames() {
        lockedWorldNames.add("art");
        lockedWorldNames.add("sandbox");
        lockedWorldNames.add("nightmare");
    }

    private void loadCreativeWorldNames() {
        creativeWorldNames.add("art");
        creativeWorldNames.add("test");
        creativeWorldNames.add("sandbox");
    }

    private static WorldStore instance;
    private Map<String, String> worldNameMap = new HashMap<>();
    private Map<String, String> worldDescMap = new HashMap<>();
    private Set<String> lockedWorldNames = new HashSet<>();
    private Set<String> creativeWorldNames = new HashSet<>();
}
