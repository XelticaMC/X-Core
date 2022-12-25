package work.xeltica.craft.core.modules.hub

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.world.WorldModule
import java.util.*

object HubModule: ModuleBase() {
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

    fun teleport(player: Player, type: HubType) {
        teleport(player, type, false)
    }

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
        val requireCooldown = bulk || Arrays.stream(noCooldownWorldNames).anyMatch { name ->
            name.equals(currentWorldName, ignoreCase = true)
        }

        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (hub.location != null) {
                player.teleportAsync(hub.getSpigotLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            } else {
                WorldModule.teleport(player, hub.worldName)
            }
            isWarpingMap.remove(player.uniqueId)
        }, if (requireCooldown) 1 else 20*5)

        if (!requireCooldown) {
            player.sendMessage("5秒後にロビーに移動します...")
            isWarpingMap[player.uniqueId] = true
        }
    }
}