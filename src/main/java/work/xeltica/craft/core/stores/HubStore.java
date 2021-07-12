package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.SignData;
import work.xeltica.craft.core.utils.Config;
import work.xeltica.craft.core.utils.LocationComparator;

public class HubStore {
    public HubStore() {
        ConfigurationSerialization.registerClass(SignData.class, "SignData");
        HubStore.instance = this;
        logger = Bukkit.getLogger();
        players = new Config("players");
        signs = new Config("signs", (s) -> {
            signData = (List<SignData>) s.getConf().getList("signs", new ArrayList<SignData>());
        });
        loadSignsFile();
        loadOrInitializeHub();
    }

    public static HubStore getInstance() {
        return HubStore.instance;
    }

    public void teleport(Player player) {
        var server = Bukkit.getServer();
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
        server.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            var world = server.getWorld(worldUuid);
            var loc = world.getSpawnLocation();
            player.teleport(loc, TeleportCause.PLUGIN);
            isWarpingMap.put(player.getUniqueId(), false);
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

    public boolean getForceAll() {
        return forceAll;
    }

    public void setForceAll(boolean value) {
        forceAll = value;
    }

    public void returnToWorld(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        var world = Bukkit.getWorld("world");
        var section = players.getConf().getConfigurationSection(player.getUniqueId().toString());
        if (section == null) {
            // はじめましての場合
            player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
            return;
        }

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
        ConfigurationSection section = players.getConf().getConfigurationSection(player.getUniqueId().toString());
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

    /**
     * プレイヤーのパラメーターを保存したものに置き換えます。
     * @param player 対象のプレイヤー。
     */
    public void restoreParams(Player player) {
        ConfigurationSection section = players.getConf().getConfigurationSection(player.getUniqueId().toString());
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

    public void writePlayerLocation(Player player) {
        var uid = player.getUniqueId().toString();
        var playersConf = players.getConf();
        var section = playersConf.getConfigurationSection(uid);
        if (section == null) {
            section = playersConf.createSection(uid);
        }
        

        section.set("location", player.getLocation());

        try {
            players.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSignsFile() {
        signs.reload();
    }

    public void saveSignsFile() throws IOException {
        signs.getConf().set("signs", signData);
        signs.save();
    }

    private void loadOrInitializeHub() {
        var world = Bukkit.getServer().getWorld("hub");
        if (world == null) {
            logger.severe("No hub found. Create it.");
            return;
        }
        worldUuid = world.getUID();
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

    private Config players;
    private Config signs;
    private Logger logger;
    private HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();
    private UUID worldUuid;
    private boolean forceAll;
    private List<SignData> signData;
}
