package work.xeltica.craft.core.stores;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.SignData;
import work.xeltica.craft.core.utils.Config;
import work.xeltica.craft.core.utils.LocationComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * クラシックロビーのコマンド看板の保存・読み出しを行います。
 * @author Xeltica
 */
public class HubStore {
    public HubStore() {
        HubStore.instance = this;

        ConfigurationSerialization.registerClass(SignData.class, "SignData");
        signs = new Config("signs", (s) -> {
            signData = (List<SignData>) s.getConf().getList("signs", new ArrayList<SignData>());
        });
        loadSignsFile();
    }

    public static HubStore getInstance() {
        return HubStore.instance;
    }

    public void teleport(Player player, HubType type) {
        teleport(player, type, false);
    }

    public void teleport(Player player, HubType hub, boolean bulk) {
        final var playerWorld = player.getWorld();
        final var world = Bukkit.getWorld(hub.getWorldName());

        if (world == null) {
            player.sendMessage("未生成");
            return;
        }

        final var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return;
        }

        if (playerWorld.getUID().equals(world.getUID())) {
            player.sendMessage("既にロビーです！");
            return;
        }

        final var currentWorldName = playerWorld.getName();
        final var requireCooldown = bulk || Arrays.stream(noCooldownWorldNames)
                .anyMatch(name -> name.equalsIgnoreCase(currentWorldName));

        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            if (hub.getLocation() != null) {
                player.teleportAsync(hub.getSpigotLocation(), TeleportCause.PLUGIN);
            } else {
                WorldStore.getInstance().teleport(player, hub.getWorldName());
            }
            isWarpingMap.put(player.getUniqueId(), false);
        }, requireCooldown ? 1 : 20 * 5);
        if (!requireCooldown) {
            player.sendMessage("5秒後にロビーに移動します...");
            isWarpingMap.put(player.getUniqueId(), true);
        }
    }

    public void returnToClassicWorld(Player player) {
        player.setGameMode(GameMode.SURVIVAL);

        WorldStore.getInstance().teleportToSavedLocation(player, "world");
    }

    public boolean processSigns(Location loc, Player player) {
        final var signData = getSignDataOf(loc);
        if (signData == null) return false;

        final var wstore = WorldStore.getInstance();

        final var cmd = signData.getCommand().toLowerCase();
        switch (cmd) {
            case "teleport" -> wstore.teleport(player, signData.getArg1());
            case "xteleport" -> wstore.teleportToSavedLocation(player, signData.getArg1());
            case "return" -> returnToClassicWorld(player);
        }
        if (cmd.equalsIgnoreCase("teleport")) {

        } else if (cmd.equalsIgnoreCase("xteleport")) {

        } else if (cmd.equalsIgnoreCase("return")) {
            returnToClassicWorld(player);
        }
        return true;
    }

    public void removeSign(Player p, Location loc) {
        final var sign = getSignDataOf(loc);
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

    public void loadSignsFile() {
        signs.reload();
    }

    public void saveSignsFile() throws IOException {
        signs.getConf().set("signs", signData);
        signs.save();
    }

    private SignData getSignDataOf(Location loc) {
        return signData.stream().filter(s -> LocationComparator.equals(loc, s.getLocation())).findFirst().orElse(null);
    }

    private final String[] noCooldownWorldNames = {
        "sandbox",
        "art",
        "pvp",
        "test",
        "hub",
        "hub2",
        "hub_dev",
        "main",
        "sandbox2",
    };

    private static HubStore instance;

    private final Config signs;
    private final HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();
    private List<SignData> signData;
}
