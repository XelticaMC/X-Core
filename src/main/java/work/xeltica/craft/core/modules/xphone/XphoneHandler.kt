package work.xeltica.craft.core.modules.xphone

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.item.ItemModule

/**
 * X Phoneに関する機能をまとめています。
 * @author Lutica
 */
class XphoneHandler : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onUse(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val itemMeta = item.itemMeta
        if (itemMeta?.displayName() == null) return
        val player = e.player

        val phone = ItemModule.getItem(ItemModule.ITEM_NAME_XPHONE)
        if (!ItemModule.compareCustomItem(item, phone)) return

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return
        e.setUseItemInHand(Event.Result.DENY)

        XphoneModule.openSpringBoard(player)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onOffhandUse(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.OFF_HAND) return
        val phone = ItemModule.getItem(ItemModule.ITEM_NAME_XPHONE)
        val item = e.player.inventory.itemInMainHand

        // 右クリック以外はガード
        if (!listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.action)) return

        // メインハンドがX Phoneであればオフハンドも使用停止
        if (ItemModule.compareCustomItem(item, phone)) {
            e.setUseItemInHand(Event.Result.DENY)
        }
    }

    @EventHandler
    fun onPlayerFirstXphone(e: PlayerTeleportEvent) {
        // ロビーから他ワールドに移動した時にX Phoneを渡す
        if (e.to.world.name == "hub2") return
        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            val record = PlayerStore.open(e.player)
            if (record.getBoolean(XphoneModule.PS_KEY_GIVEN_PHONE)) return@Runnable

            e.player.inventory.addItem(ItemModule.getItem(ItemModule.ITEM_NAME_XPHONE))
            record[XphoneModule.PS_KEY_GIVEN_PHONE] = true
        }, 1L)
    }

    @EventHandler
    fun onSneakF(e: PlayerSwapHandItemsEvent) {
        if (e.player.isSneaking) {
            e.isCancelled = true
            XphoneModule.openSpringBoard(e.player)
        }
    }
}