package work.xeltica.craft.core.modules.setMarker

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class SetMarkerHandler : Listener {

    @EventHandler
    fun onRClickMarker(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (Action.RIGHT_CLICK_BLOCK != e.action || e.hand != EquipmentSlot.HAND) return
        val clickBlock = e.clickedBlock ?: return
        //メインハンドに制限（オフハンドと2回実行されるのを回避）ブロックが取得できなければ処理終了
        val loc = clickBlock.location
        val face = e.blockFace.toString()
        Bukkit.getLogger().info("右クリックしました")
        val thisMarker = SetMarkerModule.isMarker(player, loc) //右クリックした対象がマーカーかどうか
        if (thisMarker == 1) {//他人のマーカー
            SetMarkerModule.reply(player, thisMarker)
        }
        if (thisMarker == 2) {//自分のマーカー
            if (SetMarkerModule.isMarkerTool(item)) {
                SetMarkerModule.changeActiveMarker(player, loc)
            }
        }
        if (SetMarkerModule.isMarkerToolAD(item)) {
            Bukkit.getLogger().info("ADで右クリックしました")
            if (thisMarker == 0) {
                val offsetLoc = SetMarkerModule.offset(loc, face)
                SetMarkerModule.setMarker(player, offsetLoc)
            }
        }
        if (SetMarkerModule.isMarkerToolM(item)) {
            Bukkit.getLogger().info("Mを右クリックしました")
            if (thisMarker == 0) {
                val offsetLoc = SetMarkerModule.offset(loc, face)
                SetMarkerModule.moveMarker(player, offsetLoc)
            }
        }
        Bukkit.getLogger().info("" + thisMarker)
        Bukkit.getLogger().info("" + SetMarkerModule.isMarker(player, clickBlock.location))
    }

    @EventHandler
    fun onRClickAir(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (Action.RIGHT_CLICK_AIR == e.action && e.hand == EquipmentSlot.HAND) {
            Bukkit.getLogger().info("空気を右クリックしました")
            SetMarkerModule.toolSwitching(player, item)
        }
    }

    @EventHandler
    fun onLClick(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK).contains(e.action)) {
            val clickBlock = e.clickedBlock ?: return
            val loc = clickBlock.location
            Bukkit.getLogger().info("左クリックしました")
            if (SetMarkerModule.isMarkerToolAD(item)) {
                Bukkit.getLogger().info("ADを左クリックしました")
                if ((loc.block.type == Material.REDSTONE_TORCH || loc.block.type == Material.SOUL_TORCH)) {
                    if (SetMarkerModule.isMarker(player, loc) == 2) {
                        SetMarkerModule.dellMarker(player, clickBlock.location)
                    } else if (SetMarkerModule.isMarker(player, loc) == 1) {
                        //撤去イベントキャンセル
                    }
                }
            }
        }

    }
}