package work.xeltica.craft.core.modules.setMarker

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.event.player.PlayerInteractEvent

class SetMarkerHandler: Listener {

    @EventHandler
    fun onRClickMarker(e: PlayerInteractEvent){
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (Action.RIGHT_CLICK_BLOCK == e.action && e.hand == EquipmentSlot.HAND){
            //メインハンドに制限（オフハンドと2回実行されるのを回避）
            val clickBlock = e.getClickedBlock() ?: return
            Bukkit.getLogger().info("右クリックしました")

            if(SetMarkerModule.isMarkerToolAD(item)){
                Bukkit.getLogger().info("ADを右クリックしました")
                if(clickBlock.location.block.type == Material.REDSTONE_TORCH || clickBlock.location.block.type == Material.SOUL_TORCH){
                    SetMarkerModule.isClickMarker(player, clickBlock.location)
                    Bukkit.getLogger().info("トーチを右クリックしました")
                }else{
                    val loctemp = Location(player.getWorld(), clickBlock.location.x, clickBlock.location.y +1, clickBlock.location.z)
                    SetMarkerModule.setMarker(player, loctemp)
                    Bukkit.getLogger().info("それ以外を右クリックしました")
                }
            }
            if(SetMarkerModule.isMarkerToolM(item)){
                Bukkit.getLogger().info("Mを右クリックしました")
                if(clickBlock.location.block.type == Material.REDSTONE_TORCH || clickBlock.location.block.type == Material.SOUL_TORCH){
                    SetMarkerModule.isClickMarker(player, clickBlock.location)
                    Bukkit.getLogger().info("トーチを右クリックしました")
                }else{
                    SetMarkerModule.moveMaker(player, clickBlock.location)
                    Bukkit.getLogger().info("それ以外を右クリックしました")
                }
            }
            Bukkit.getLogger().info(""+SetMarkerModule.isClickMarker(player, clickBlock.location))
        }
    }

    @EventHandler
    fun onRClickAir(e: PlayerInteractEvent){
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (Action.RIGHT_CLICK_AIR == e.action && e.hand == EquipmentSlot.HAND){
            Bukkit.getLogger().info("空気を右クリックしました")
            SetMarkerModule.toolSwitching(player,item)
        }
    }

    @EventHandler
    fun onLClick(e: PlayerInteractEvent){
        val player = e.player
        val item = e.player.inventory.itemInMainHand
        if (listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK).contains(e.action)) {
            val clickBlock = e.getClickedBlock() ?: return
            val loc = clickBlock.location
            Bukkit.getLogger().info("左クリックしました")
            if(SetMarkerModule.isMarkerToolAD(item)) {
                Bukkit.getLogger().info("ADを左クリックしました")
                if ( (loc.block.type == Material.REDSTONE_TORCH || loc.block.type == Material.SOUL_TORCH)) {
                    if(SetMarkerModule.isClickMarker(player,loc,false) == 2){
                        SetMarkerModule.dellMarker(player, clickBlock.location)
                    }else if(SetMarkerModule.isClickMarker(player,loc,false) == 1){
                        //撤去イベントキャンセル
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSignClick(e: PlayerInteractEvent) {
        // 右クリックでない or スニークしていない なら中止
        if (e.action !== Action.RIGHT_CLICK_BLOCK || !e.player.isSneaking) return
        // 既にキャンセルされているなら中止
        if (e.useInteractedBlock() == Event.Result.DENY) return
        // クリックしたブロックが看板でなければ中止
        val sign = e.clickedBlock?.state as? Sign ?: return

        e.player.openSign(sign)
    }
}