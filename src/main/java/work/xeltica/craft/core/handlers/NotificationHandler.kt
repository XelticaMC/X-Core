package work.xeltica.craft.core.handlers

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.stores.NotificationStore
import work.xeltica.craft.core.utils.Ticks
import work.xeltica.craft.core.xphone.XphoneOs

class NotificationHandler : Listener {
    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        object : BukkitRunnable() {
            override fun run() {
                val unreadNotifications = NotificationStore.getInstance().getUnreadNotification(e.player)
                if (unreadNotifications.isEmpty()) return
                e.player.sendMessage("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}${unreadNotifications.count()}件の通知があります！！")
                unreadNotifications.forEach {
                    e.player.sendMessage("${ChatColor.GREEN}・${ChatColor.RESET}${it.title}")
                }
                e.player.sendMessage("${ChatColor.LIGHT_PURPLE}X Phoneの「通知アプリ」から確認できます。")
                XphoneOs.playTritone(e.player)
            }
        }.runTaskLater(XCorePlugin.instance, Ticks.from(5.0).toLong())
    }
}