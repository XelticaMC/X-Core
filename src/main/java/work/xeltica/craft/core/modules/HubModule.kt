package work.xeltica.craft.core.modules

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import work.xeltica.craft.core.models.HubType
import work.xeltica.craft.core.XCorePlugin
import java.lang.Runnable
import work.xeltica.craft.core.stores.WorldStore
import java.util.*

/**
 * ロビーへのテレポートを管理します。
 * @author Xeltica
 */
object HubModule : ModuleBase() {
    @JvmStatic
    fun teleport(player: Player, type: HubType) {
        teleport(player, type, false)
    }

    @JvmStatic
    fun teleport(player: Player, hub: HubType, bulk: Boolean) {
        val playerWorld = player.world
        val world = Bukkit.getWorld(hub.worldName)
        if (world == null) {
            player.sendMessage("未生成")
            return
        }
        val isWarping = isWarpingMap[player.uniqueId]
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！")
            return
        }
        if (playerWorld.uid == world.uid) {
            player.sendMessage("既にロビーです！")
            return
        }
        val currentWorldName = playerWorld.name
        val requireCooldown = bulk || Arrays.stream(noCooldownWorldNames)
            .anyMatch { name: String -> name.equals(currentWorldName, ignoreCase = true) }
        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (hub.location != null) {
                player.teleportAsync(hub.spigotLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
            } else {
                WorldStore.getInstance().teleport(player, hub.worldName)
            }
            isWarpingMap[player.uniqueId] = false
        }, if (requireCooldown) 1 else 20 * 5.toLong())
        if (!requireCooldown) {
            player.sendMessage("5秒後にロビーに移動します...")
            isWarpingMap[player.uniqueId] = true
        }
    }

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
}