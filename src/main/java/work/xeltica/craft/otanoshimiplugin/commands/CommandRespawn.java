package work.xeltica.craft.otanoshimiplugin.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.otanoshimiplugin.OtanoshimiPlugin;

public class CommandRespawn extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var worldName = player.getWorld().getName();
        if (worldName.equals("hub") || worldName.equals("sandbox")) {
            player.sendMessage(ChatColor.RED + "このワールドでは許可されていません");
            return true;
        }
        // TODO: 他の箇所もそうだけど、メインワールドのハードコーディングやめたいかも
        var world = Bukkit.getWorld("world");
        var respawn = world.getSpawnLocation();

        var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(respawn, TeleportCause.PLUGIN);
                isWarpingMap.put(player.getUniqueId(), false);
            }
        }.runTaskLater(OtanoshimiPlugin.getInstance(), 20 * 5);
        player.sendMessage("5秒後に初期スポーンに移動します...");
        isWarpingMap.put(player.getUniqueId(), true);

        return true;
    }
    
    private HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();

}
