package work.xeltica.craft.core.handlers

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import work.xeltica.craft.core.modules.CustomItemModule
import work.xeltica.craft.core.modules.XphoneModule

/**
 * X Phoneに関する機能をまとめています。
 * @author Xeltica
 */
class XphoneHandler : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onUse(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val itemMeta = item.itemMeta
        if (itemMeta?.displayName() == null) return
        val player = e.player

        val phone = CustomItemModule.getItem(CustomItemModule.ITEM_NAME_XPHONE)
        if (!CustomItemModule.compareCustomItem(item, phone)) return

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return
        e.setUseItemInHand(Event.Result.DENY)

        XphoneModule.openSpringBoard(player)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onOffhandUse(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.OFF_HAND) return
        val phone = CustomItemModule.getItem(CustomItemModule.ITEM_NAME_XPHONE)
        val item = e.player.inventory.itemInMainHand

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return

        // メインハンドがX Phoneであればオフハンドも使用停止
        if (CustomItemModule.compareCustomItem(item, phone)) {
            e.setUseItemInHand(Event.Result.DENY)
        }
    }
}