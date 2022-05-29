package work.xeltica.craft.core.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.stores.WorldStore;

/**
 * 初期スポーンに転送するコマンド
 * @author Xeltica
 */
public class CommandRespawn extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        // テレポート中であれば弾く
        final var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return true;
        }

        isWarpingMap.put(player.getUniqueId(), true);

        // 第一引数の内容によってテレポート先を分岐
        if (args.length > 0 && args[0].equalsIgnoreCase("bed")) {
            teleportToBedSpawn(player);
        } else {
            teleportToInitialSpawn(player);
        }
        return true;
    }

    /**
     * ベッド位置にリスポーンします
     * @param player リスポーンさせるプレイヤー
     */
    private void teleportToBedSpawn(Player player) {
        if (WorldStore.getInstance().getRespawnWorld(player.getWorld()) == null) {
            player.sendMessage(ChatColor.RED + "このワールドでは許可されていません");
            isWarpingMap.put(player.getUniqueId(), false);
            return;
        }

        final var loc = player.getBedSpawnLocation();
        if (loc == null) {
            player.sendMessage("ベッドが存在しないか、塞がれているためにテレポートできません。");
            isWarpingMap.put(player.getUniqueId(), false);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleportAsync(loc, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 5);
        player.sendMessage("5秒後にベッドの位置にテレポートします...");
    }

    /**
     * ワールドの初期スポーンにテレポートします
     * @param player テレポートさせるプレイヤー
     */
    private void teleportToInitialSpawn(Player player) {
        final var respawnWorldName = WorldStore.getInstance().getRespawnWorld(player.getWorld());
        if (respawnWorldName == null) {
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
                player.teleportAsync(respawn, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 5);

        player.sendMessage(isSameWorld
            ? "5秒後に初期スポーンに移動します..."
            : "5秒後に" + respawnWorldDisplayName + "の初期スポーンに移動します..."
        );
    }

    private final HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        return COMMANDS;
    }

    private static final List<String> COMMANDS = List.of("bed");

}
