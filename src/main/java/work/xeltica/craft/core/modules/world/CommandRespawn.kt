package work.xeltica.craft.core.modules.world

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.utils.Ticks
import java.util.UUID

/**
 * 初期スポーンに転送するコマンド
 * @author Xeltica
 */
class CommandRespawn : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        // テレポート中であれば弾く
        val isWarping = isWarpingMap[player.uniqueId]
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！")
            return true
        }

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
        val world = WorldModule.getWorldInfo(player.world)
        if (!world.canRespawn) {
            player.sendMessage(ChatColor.RED.toString() + "このワールドでは許可されていません")
            return
        }
        val loc = player.bedSpawnLocation
        if (loc == null) {
            player.sendMessage("ベッドが存在しないか、塞がれているためにテレポートできません。")
            return
        }
        object : BukkitRunnable() {
            override fun run() {
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                isWarpingMap[player.uniqueId] = false
            }
        }.runTaskLater(instance, Ticks.from(5.0).toLong())
        player.sendMessage("5秒後にベッドの位置にテレポートします...")
        isWarpingMap[player.uniqueId] = true
    }

    /**
     * ワールドの初期スポーンにテレポートします
     * @param player テレポートさせるプレイヤー
     */
    private fun teleportToInitialSpawn(player: Player) {
        val world = WorldModule.getWorldInfo(player.world)
        if (!world.canSleep) {
            player.sendMessage(ChatColor.RED.toString() + "このワールドでは許可されていません")
            return
        }
        val respawnWorld = Bukkit.getWorld(world.respawnWorld)
        val respawn = respawnWorld!!.spawnLocation
        val isSameWorld = player.world.uid == respawnWorld.uid
        if (noCooldownWorldNames.contains(respawnWorld.name)) {
            player.teleportAsync(respawn, PlayerTeleportEvent.TeleportCause.PLUGIN)
            return
        }
        object : BukkitRunnable() {
            override fun run() {
                player.teleportAsync(respawn, PlayerTeleportEvent.TeleportCause.PLUGIN)
                isWarpingMap[player.uniqueId] = false
            }
        }.runTaskLater(instance, Ticks.from(5.0).toLong())
        player.sendMessage(
            if (isSameWorld)
                "5秒後に初期スポーンに移動します..."
            else
                "5秒後に${WorldModule.getWorldInfo(world.respawnWorld).displayName}の初期スポーンに移動します..."
        )
        isWarpingMap[player.uniqueId] = true
    }

    private val isWarpingMap = HashMap<UUID, Boolean>()
    private val noCooldownWorldNames = listOf(
        "art",
        "pvp",
        "test",
        "hub2",
        "hub_dev",
        "main",
        "sandbox2",
        "event"
    )

    override fun onTabComplete(
        commandSender: CommandSender, command: Command, label: String,
        args: Array<String>,
    ): List<String> {
        return COMMANDS
    }

    private val COMMANDS = listOf("bed")
}