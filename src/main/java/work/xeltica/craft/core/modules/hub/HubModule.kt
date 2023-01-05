package work.xeltica.craft.core.modules.hub

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.utils.Ticks
import java.util.UUID

object HubModule : ModuleBase() {
    private val noCooldownWorldNames = arrayOf(
        "art",
        "pvp",
        "test",
        "hub2",
        "hub_dev",
        "main",
        "sandbox2"
    )

    private val isWarpingMap = HashMap<UUID, Boolean>()

    override fun onEnable() {
        registerCommand("hub", HubCommand())
        registerHandler(HubHandler())
    }

    /**
     * [player] をタイプ: [type] のロビーに転送します。
     */
    fun teleport(player: Player, type: HubType) {
        teleport(player, type, false)
    }

    /**
     * [player] をタイプ: [hub] のロビーに転送します。
     * [bulk] がtrueの場合、クールダウンタイムをスキップします。
     */
    fun teleport(player: Player, hub: HubType, bulk: Boolean) {
        val playerWorld = player.world
        val world = Bukkit.getWorld(hub.worldName)

        if (world == null) {
            player.sendMessage("未生成")
            return
        }

        val isWarping = isWarpingMap[player.uniqueId]
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です!")
            return
        }

        if (playerWorld.uid == world.uid) {
            player.sendMessage("既にロビーです!")
            return
        }

        val currentWorldName = playerWorld.name
        val skipCooldown = bulk || noCooldownWorldNames.any {
            it.equals(currentWorldName, ignoreCase = true)
        }

        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (hub.location != null) {
                // 位置指定がある
                player.teleportAsync(hub.getSpigotLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            } else {
                // 位置指定がない
                WorldModule.teleport(player, hub.worldName)
            }
            isWarpingMap.remove(player.uniqueId)
        }, if (skipCooldown) 1 else Ticks.from(5.0).toLong())

        if (!skipCooldown) {
            player.sendMessage("5秒後にロビーに移動します...")
            isWarpingMap[player.uniqueId] = true
        }
    }
}