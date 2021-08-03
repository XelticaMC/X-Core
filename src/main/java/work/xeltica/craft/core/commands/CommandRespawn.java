package work.xeltica.craft.core.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.stores.WorldStore;

/**
 * 初期スポーンに転送するコマンド
 * @author Xeltica
 */
public class CommandRespawn extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("bed")) {
            teleportToBedSpawn(player);
        } else {
            teleportToInitialSpawn(player);
        }

        isWarpingMap.put(player.getUniqueId(), true);
        return true;
    }

    private void teleportToBedSpawn(Player player) {
        try {
            // respawn禁止されているかどうかの検証
            getRespawnWorld(player.getWorld());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "このワールドでは許可されていません");
            return;
        }

        final var loc = player.getBedSpawnLocation();

        if (loc == null) {
            player.sendMessage("ベッドが存在しないか、塞がれているためにテレポートできません。");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 5);
        player.sendMessage("5秒後にベッドの位置にテレポートします...");
    }

    private void teleportToInitialSpawn(Player player) {
        final String respawnWorldName;
        try {
            respawnWorldName = getRespawnWorld(player.getWorld());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "このワールドでは許可されていません");
            return;
        }
        final var respawnWorld = Bukkit.getWorld(respawnWorldName);
        final var respawn =  respawnWorld.getSpawnLocation();

        final var isSameWorld = player.getWorld().getUID().equals(respawnWorld.getUID());
        final var respawnWorldDisplayName = WorldStore.getInstance().getWorldDisplayName(respawnWorld);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(respawn, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 5);

        player.sendMessage(isSameWorld
            ? "5秒後に初期スポーンに移動します..."
            : "5秒後に" + respawnWorldDisplayName + "の初期スポーンに移動します..."
        );
    }

    private String getRespawnWorld(World w) throws Exception {
        // TODO 旅行券のときに位置情報保存しておいてーとかそういう処理に対応したい
        if (w.getName().startsWith("travel_")) return "world";
        return switch (w.getName()) {
            default -> w.getName();

            case "wildarea2_nether" -> "wildarea2";
            case "wildarea2_the_end" -> "wildarea2";

            case "world_nether" -> "world";
            case "world_the_end" -> "world";
            case "wildarea" -> "world";
            case "nightmare" -> "world";

            case "pvp" -> throw new Exception();
        };
    }

    private final HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();

}
