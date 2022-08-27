package work.xeltica.craft.core.modules.notification

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.utils.Ticks

class NotificationHandler : Listener {
    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        object : BukkitRunnable() {
            override fun run() {
                NotificationModule.pushNotificationTo(e.player)
            }
        }.runTaskLater(XCorePlugin.instance, Ticks.from(5.0).toLong())
    }
}