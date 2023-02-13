package work.xeltica.craft.core.modules.setMarker

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class SetMarkerHandler : Listener {

    @EventHandler
    fun onRightClick(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        // メインハンドに制限（オフハンドと2回実行されるのを回避）
        if (Action.RIGHT_CLICK_BLOCK != e.action || e.hand != EquipmentSlot.HAND) return
        // ブロックが取得できなければ処理終了
        val clickBlock = e.clickedBlock ?: return
        val loc = clickBlock.location
        val face = e.blockFace.toString()
        val thisMarker = SetMarkerModule.isMarker(player, loc) // 右クリックした対象がマーカーかどうか
        if (thisMarker == 1) { // 他人のマーカー
            player.sendMessage("あなたのマーカーではないようです…")
            return
        }
        if (thisMarker == 2) { // 自分のマーカー
            if (SetMarkerModule.isMarkerTool(item)) { // 専用ツールであるかどうか
                SetMarkerModule.changeActiveMarker(player, loc)
                return
            }
        }
        //マーカー以外のブロック
        if (thisMarker == 0) {
            if (SetMarkerModule.isMarkerToolAD(item)) { // 追加ツール動作
                SetMarkerModule.setMarker(player, loc, face)
                return
            }
            if (SetMarkerModule.isMarkerToolM(item)) { // 移動ツール動作
                SetMarkerModule.moveMarker(player, loc, face)
                return
            }
        }
    }

    @EventHandler
    fun onRightClickAir(e: PlayerInteractEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (Action.RIGHT_CLICK_AIR == e.action && e.hand == EquipmentSlot.HAND) {
            SetMarkerModule.toolSwitching(player, item)
        }
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        val loc = e.block.location
        val thisMarker = SetMarkerModule.isMarker(player, loc)
        // そもそもマーカーではない
        if (thisMarker == 0) return
        // 自分のマーカーでないのでイベントキャンセル
        if (thisMarker == 1) {
            player.sendMessage("破壊に失敗しました。あなたのマーカーではないようです…")
            e.isCancelled = true
            return
        } // 自分のマーカーである
        if (thisMarker == 2) {
            //もしツールが追加・削除モードでない時
            if (!SetMarkerModule.isMarkerToolAD(item)) {
                player.sendMessage("破壊する場合は追加・削除ツールで破壊してください。")
                e.isCancelled = true
                return
            }
            // 追加・削除モードであった時
            val flag = SetMarkerModule.deleteMarker(player, loc)
            // 処理に失敗した場合：これが実行されることはないと信じたい。神様ぁ
            if (!flag) {
                player.sendMessage("破壊もしくは削除に失敗しました。")
                e.isCancelled = true
            }
        }
    }
}