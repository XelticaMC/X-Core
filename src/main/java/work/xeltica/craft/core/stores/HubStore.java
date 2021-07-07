package work.xeltica.craft.core.stores;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import work.xeltica.craft.core.generators.chunks.EmptyChunkGenerator;
import work.xeltica.craft.core.models.SignData;
import work.xeltica.craft.core.utils.LocationComparator;

public class HubStore {
    public HubStore(Plugin pl) {
        ConfigurationSerialization.registerClass(SignData.class, "SignData");
        this.plugin = pl;
        HubStore.instance = this;
        logger = pl.getLogger();
        loadWorld();
        reloadStore();
    }

    public static HubStore getInstance() {
        return HubStore.instance;
    }

    public void teleport(Player player) {
        var server = plugin.getServer();
        if (worldUuid == null) {
            player.sendMessage("hub が未生成");
            return;
        }
        var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return;
        }
        if (player.getWorld().getUID().equals(worldUuid)) {
            player.sendMessage("既にロビーです！");
            return;
        }
        var worldName = player.getWorld().getName();
        var isSaveIgnoredWorld = Arrays.stream(inventorySaverIgnoredWorldNames)
                .anyMatch(name -> name.equalsIgnoreCase(worldName));
        server.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                var world = server.getWorld(worldUuid);
                var loc = world.getSpawnLocation();
                var savePosition = !worldName.equalsIgnoreCase("nightmare");
                // 砂場から行く場合は記録しない & ポーション効果を潰す
                if (!isSaveIgnoredWorld) {
                    writePlayerConfig(player, savePosition);
                    playersConf = YamlConfiguration.loadConfiguration(playersConfFile);
                } else {
                    // ポーション効果削除
                    player.getActivePotionEffects().stream().forEach(e -> player.removePotionEffect(e.getType()));
                }
                player.getInventory().clear();
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFoodLevel(20);
                player.setSaturation(0);
                player.setExhaustion(0);
                player.setLevel(0);
                player.setExp(0);
                player.setFireTicks(0);

                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(loc, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }, isSaveIgnoredWorld ? 1 : 20 * 5);
        if (!isSaveIgnoredWorld) {
            player.sendMessage("5秒後にロビーに移動します...");
            isWarpingMap.put(player.getUniqueId(), true);
        }
    }

    public UUID getHubId() {
        return worldUuid;
    }

    public World getHub() {
        return Bukkit.getWorld(worldUuid);
    }

    public void CreateHub() {
        var world = new WorldCreator("hub").environment(Environment.NORMAL).generator(new EmptyChunkGenerator()).createWorld();
        configureRule(world);
        world.getBlockAt(0, 60, 0).setType(Material.BIRCH_LOG);
    }

    public boolean tryUnload() {
        if (worldUuid == null) return false;

        var world = Bukkit.getWorld(worldUuid);
        if (world == null) return false;

        Bukkit.unloadWorld(world, true);
        return true;
    }

    public boolean tryUpdate() {
        if (worldUuid == null) return false;

        var world = Bukkit.getWorld(worldUuid);
        if (world == null) return false;

        configureRule(world);
        return true;
    }

    public boolean getForceAll() {
        return forceAll;
    }

    public void setForceAll(boolean value) {
        forceAll = value;
    }

    public void returnToWorld(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        var world = Bukkit.getWorld("world");
        ConfigurationSection section = playersConf.getConfigurationSection(player.getUniqueId().toString());
        if (section == null) {
            // はじめましての場合
            player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
            return;
        }

        restoreInventory(player);
        restoreParams(player);

        var locationResult = section.get("location");
        if (locationResult == null || !(locationResult instanceof Location)) {
            player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
            player.sendMessage("§c最後にいた場所が記録されていないため、初期スポーンにワープします。これはおそらくバグなので、管理者に報告してください。 Code: 2");
            return;
        }

        var loc = (Location) locationResult;
        player.teleport(loc, TeleportCause.PLUGIN);
    }

    public boolean processSigns(Location loc, Player p) {
        var signData = getSignDataOf(loc);
        if (signData == null) return false;

        var cmd = signData.getCommand();
        if (cmd.equalsIgnoreCase("teleport")) {
            var worldName = signData.getArg1();
            WorldStore.getInstance().teleport(p, worldName);
        } else if (cmd.equalsIgnoreCase("return")) {
            returnToWorld(p);
        }
        return true;
    }

    public void removeSign(Player p, Location loc) {
        var sign = getSignDataOf(loc);
        if (sign != null) {
            signData.remove(sign);
            try {
                saveSignsFile();
                p.sendMessage("看板を撤去しました。");
            } catch (IOException e1) {
                p.sendMessage("§b看板の撤去に失敗しました。");
                e1.printStackTrace();
            }
        }
    }

    public void placeSign(Player p, Location loc, String command, String arg1, String arg2) {
        signData.add(new SignData(loc, command, arg1, arg2));
        try {
            saveSignsFile();
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1, 1);
            p.sendMessage("設置に成功しました。");
        } catch (IOException e1) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 0.5f);
            p.sendMessage("内部エラー発生。");
            e1.printStackTrace();
        }
    }

    public void restoreInventory(Player player) {
        ConfigurationSection section = playersConf.getConfigurationSection(player.getUniqueId().toString());
        if (section == null)
            return;

        var inv = player.getInventory();
        inv.clear();
        var itemsResult = section.get("items");
        if (itemsResult == null) {
            player.sendMessage("§cインベントリ復元失敗。これはおそらくバグなので、管理者に報告してください。 Code: 1");
        } else {
            var items = (ArrayList<ItemStack>) itemsResult;
            for (var i = 0; i < items.size(); i++) {
                inv.setItem(i, items.get(i));
            }
        }
    }

    public void restoreParams(Player player) {
        ConfigurationSection section = playersConf.getConfigurationSection(player.getUniqueId().toString());
        if (section == null)
            return;

        player.setHealth(section.getDouble("health", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        // TODO: 満腹度上限の決め打ちをしなくて済むならそうしたい
        player.setFoodLevel(section.getInt("foodLevel", 20));
        player.setSaturation((float) section.getDouble("saturaton", 0));
        player.setExhaustion((float) section.getDouble("exhaustion", 0));
        player.setExp((float) section.getDouble("exp", 0));
        player.setLevel(section.getInt("level", 0));
        player.setFireTicks(section.getInt("fire", 0));
    }

    public void reloadPlayers() {
        playersConf = YamlConfiguration.loadConfiguration(playersConfFile);
    }
    
    public void reloadStore() {
        playersConfFile = new File(plugin.getDataFolder(), "players.yml");
        signsConfFile = new File(plugin.getDataFolder(), "signs.yml");
        playersConf = YamlConfiguration.loadConfiguration(playersConfFile);
        signsConf = YamlConfiguration.loadConfiguration(signsConfFile);
        signData = (List<SignData>) signsConf.getList("signs", new ArrayList<SignData>());
    }

    public void writePlayerConfig(Player player, boolean savesLocation) {
        writePlayerConfig(player, savesLocation, true);
    }

    public void writePlayerConfig(Player player, boolean savesLocation, boolean savesParams) {
        var uid = player.getUniqueId().toString();
        var section = playersConf.getConfigurationSection(uid);
        if (section == null) {
            section = playersConf.createSection(uid);
        }

        // 座標を記録
        if (savesLocation) {
            section.set("location", player.getLocation());
        }

        // インベントリを記録
        var inv = player.getInventory();
        var items = new ItemStack[inv.getSize()];
        for (var i = 0; i < items.length; i++) {
            items[i] = inv.getItem(i);
        }
        section.set("items", items);

        // 体力、満腹度、レベル、炎状態を記録
        section.set("health", savesParams ? player.getHealth() : null);
        section.set("foodLevel", savesParams ? player.getFoodLevel() : null);
        section.set("saturaton", savesParams ? player.getSaturation() : null);
        section.set("exhaustion", savesParams ? player.getExhaustion() : null);
        section.set("exp", savesParams ? player.getExp() : null);
        section.set("level", savesParams ? player.getLevel() : null);
        section.set("fire", savesParams ? player.getFireTicks() : null);

        try {
            logger.info(playersConf.toString());
            playersConf.save(playersConfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        playersConf = YamlConfiguration.loadConfiguration(playersConfFile);
    }

    public void loadSignsFile() {
        signsConf = YamlConfiguration.loadConfiguration(signsConfFile);
        signData = (List<SignData>) signsConf.getList("signs", new ArrayList<SignData>());
    }

    public void saveSignsFile() throws IOException {
        signsConf = YamlConfiguration.loadConfiguration(signsConfFile);
        signsConf.set("signs", signData);
        signsConf.save(signsConfFile);
        loadSignsFile();
    }

    private void loadWorld() {
        var world = plugin.getServer().getWorld("hub");
        if (world == null) {
            logger.info("Generating Hub...");
            CreateHub();
        }
        worldUuid = world.getUID();
        world.getPlayers().stream().forEach(player -> {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
            player.setSaturation(0);
            player.setExhaustion(0);
            player.setLevel(0);
            player.setExp(0);
            player.setFireTicks(0);
        });
    }

    private void configureRule(World world) {
        world.setSpawnLocation(0, 65, 0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setDifficulty(Difficulty.PEACEFUL);
    }

    private SignData getSignDataOf(Location loc) {
        return signData.stream().filter(s -> LocationComparator.equals(loc, s.getLocation())).findFirst().orElse(null);
    }

    private final String[] inventorySaverIgnoredWorldNames = {
        "sandbox",
        "art", 
        "pvp",
        "test",
    };

    private static HubStore instance;
    private Plugin plugin;

    private File playersConfFile;
    private YamlConfiguration playersConf;
    private File signsConfFile;
    private YamlConfiguration signsConf;
    private Logger logger;
    private HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();
    private UUID worldUuid;
    private boolean forceAll;
    private List<SignData> signData;
}
