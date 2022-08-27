package work.xeltica.craft.core.modules.notification

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.Notification
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.xphone.apps.AppBase

class NotificationApp: AppBase() {
    override fun getName(player: Player): String {
        val count = NotificationModule.getUnreadNotification(player).count()
        return if (count > 0) "通知（${count}）" else "通知"
    }

    override fun getIcon(player: Player): Material = Material.REDSTONE

    override fun isShiny(player: Player): Boolean = NotificationModule.getUnreadNotification(player).isNotEmpty()

    override fun onLaunch(player: Player) {
        val ui = Gui.getInstance()
        val list = mutableListOf<MenuItem>()
        val notifications = NotificationModule.getUnreadNotification(player)
        if (notifications.isEmpty()) {
            ui.error(player, "新しい通知はありません。")
            return
        }
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
        if (notification.giftItems != null) {
            if (!giftAllowedWorlds.contains(player.world.name)) {
                Gui.getInstance().error(player, "メインワールドとは独立したインベントリを持つワールドにいるため、アイテムを受け取ることができません。\nメインワールドに移動してから通知を既読してください。")
                return
            }
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            for (item in notification.giftItems) {
                player.inventory.addItem(item)
                player.sendMessage("${PlainTextComponentSerializer.plainText().serialize(item.displayName())} を受け取りました。")
            }
        }
        if (notification.ep != null) {
            EbiPowerStore.getInstance().tryGive(player, notification.ep)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            player.sendMessage("${notification.ep} EPを受け取りました。")
        }
        NotificationModule.readNotification(player, notification)
    }

    private val giftAllowedWorlds = arrayOf(
        "main",
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "wildareab",
        "shigen_nether",
        "shigen_end",
    )
}