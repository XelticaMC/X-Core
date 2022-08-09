package work.xeltica.craft.core.handlers

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import work.xeltica.craft.core.stores.ItemStore
import work.xeltica.craft.core.services.XphoneService

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

        val phone = store().getItem(ItemStore.ITEM_NAME_XPHONE)
        if (!store().compareCustomItem(item, phone)) return

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return
        e.setUseItemInHand(Event.Result.DENY)

        XphoneService.openSpringBoard(player)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onOffhandUse(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.OFF_HAND) return
        val phone = store().getItem(ItemStore.ITEM_NAME_XPHONE)
        val item = e.player.inventory.itemInMainHand

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return

        // メインハンドがX Phoneであればオフハンドも使用停止
        if (store().compareCustomItem(item, phone)) {
            e.setUseItemInHand(Event.Result.DENY)
        }
    }

    private fun store(): ItemStore {
        return ItemStore.getInstance()
    }
}