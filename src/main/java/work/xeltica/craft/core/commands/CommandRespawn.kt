package work.xeltica.craft.core.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.stores.WorldStore
import work.xeltica.craft.core.XCorePlugin
import java.util.UUID
import java.util.HashMap

/**
 * 初期スポーンに転送するコマンド
 * @author Xeltica
 */
class CommandRespawn : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        // テレポート中であれば弾く
        val isWarping = isWarpingMap[player.uniqueId]
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！")
            return true
        }
        isWarpingMap[player.uniqueId] = true

        // 第一引数の内容によってテレポート先を分岐
        if (args.isNotEmpty() && args[0].equals("bed", ignoreCase = true)) {
            teleportToBedSpawn(player)
        } else {
            teleportToInitialSpawn(player)
        }
        return true
    }

    /**
     * ベッド位置にリスポーンします
     * @param player リスポーンさせるプレイヤー
     */
    private fun teleportToBedSpawn(player: Player) {
        if (WorldStore.getInstance().getRespawnWorld(player.world) == null) {
            player.sendMessage(ChatColor.RED.toString() + "このワールドでは許可されていません")
            isWarpingMap[player.uniqueId] = false
            return
        }
        val loc = player.bedSpawnLocation
        if (loc == null) {
            player.sendMessage("ベッドが存在しないか、塞がれているためにテレポートできません。")
            isWarpingMap[player.uniqueId] = false
            return
        }
        object : BukkitRunnable() {
            override fun run() {
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                isWarpingMap[player.uniqueId] = false
            }
        }.runTaskLater(XCorePlugin.getInstance(), (20 * 5).toLong())
        player.sendMessage("5秒後にベッドの位置にテレポートします...")
    }

    /**
     * ワールドの初期スポーンにテレポートします
     * @param player テレポートさせるプレイヤー
     */
    private fun teleportToInitialSpawn(player: Player) {
        val respawnWorldName = WorldStore.getInstance().getRespawnWorld(player.world)
        if (respawnWorldName == null) {
            player.sendMessage(ChatColor.RED.toString() + "このワールドでは許可されていません")
            return
        }
        val respawnWorld = Bukkit.getWorld(respawnWorldName)
        val respawn = respawnWorld!!.spawnLocation
        val isSameWorld = player.world.uid == respawnWorld.uid
        val respawnWorldDisplayName = WorldStore.getInstance().getWorldDisplayName(respawnWorld)
        object : BukkitRunnable() {
            override fun run() {
                player.teleportAsync(respawn, PlayerTeleportEvent.TeleportCause.PLUGIN)
                isWarpingMap[player.uniqueId] = false
            }
        }.runTaskLater(XCorePlugin.getInstance(), (20 * 5).toLong())
        player.sendMessage(
            if (isSameWorld) "5秒後に初期スポーンに移動します..." else "5秒後に" + respawnWorldDisplayName + "の初期スポーンに移動します..."
        )
    }

    private val isWarpingMap = HashMap<UUID, Boolean>()
    override fun onTabComplete(
        commandSender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): List<String> {
        return COMMANDS
    }

    companion object {
        private val COMMANDS = listOf("bed")
    }
}