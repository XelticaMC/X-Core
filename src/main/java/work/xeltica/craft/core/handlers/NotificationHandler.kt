package work.xeltica.craft.core.handlers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Ticks
import work.xeltica.craft.core.modules.NotificationModule

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