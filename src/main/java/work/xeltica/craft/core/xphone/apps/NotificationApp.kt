package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.Notification
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.stores.NotificationStore

class NotificationApp: AppBase() {
    override fun getName(player: Player): String = "通知"

    override fun getIcon(player: Player): Material = Material.REDSTONE

    override fun onLaunch(player: Player) {
        val ui = Gui.getInstance()
        val list = mutableListOf<MenuItem>()
        val notifications = NotificationStore.getInstance().getUnreadNotification(player)
        val icon = fun(n: Notification): Material = if (n.giftItems != null) Material.CHEST_MINECART else Material.MINECART
        for (notification in notifications) {
            list.add(
                MenuItem(notification.title, {
                    ui.openDialog(player, notification.title, notification.message, { readNotification(player, notification) }, "確認")
                }, icon.invoke(notification))
            )
        }
        ui.openMenu(player, "通知", list)
    }

    private fun readNotification(player: Player, notification: Notification) {
        if (notification.ep != null) {
            EbiPowerStore.getInstance().tryGive(player, notification.ep)
            player.sendMessage("ep: " + notification.ep)
        }
        if (notification.giftItems != null) {
            for (item in notification.giftItems) {
                player.inventory.addItem(item)
                player.sendMessage(item.displayName())
            }
        }
        NotificationStore.getInstance().readNotification(player, notification)
    }
}