package work.xeltica.craft.core.modules.item

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.generator.structure.StructureType
import work.xeltica.craft.core.gui.Gui.Companion.getInstance

class TicketWildareaBHandler : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onUse(e: PlayerInteractEvent) {
        val store = ItemModule
        val item = e.item ?: return
        val itemMeta = item.itemMeta
        if (itemMeta?.displayName() == null) return
        val player = e.player
        val ticket = store.getItem(ItemModule.ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT)

        // 右クリック以外はガード
        if (e.action !in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return
        if (!store.compareCustomItem(item, ticket)) {
            return
        }
        e.setUseItemInHand(Event.Result.DENY)
        val wildareab = Bukkit.getWorld("wildareab")
        if (wildareab == null) {
            getInstance().error(player, "テレポートに失敗しました。ワールドが生成されていません。")
            return
        }
        if (player.gameMode != GameMode.CREATIVE) {
            item.amount = item.amount - 1
        }
        player.sendMessage("旅行券を使用しました。現在手配中です。その場で少しお待ちください！")
        for (pl in Bukkit.getOnlinePlayers()) {
            pl.sendMessage(String.format("§6%s§rさんが資源ワールド:海底神殿への旅に行きます！§b行ってらっしゃい！", player.displayName))
        }
        val structure = wildareab.locateNearestStructure(wildareab.spawnLocation, StructureType.OCEAN_MONUMENT, 200, true)
        if (structure == null) {
            getInstance().error(player, "みつかりませんでした。")
            return
        }
        val loc = structure.location
        loc.y = 64.0
        loc.block.type = Material.STONE
        loc.y = 65.0
        player.teleportAsync(loc)
    }
}