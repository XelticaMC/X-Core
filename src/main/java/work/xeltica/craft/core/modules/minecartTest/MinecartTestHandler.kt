package work.xeltica.craft.core.modules.minecartTest

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEnterEvent
import work.xeltica.craft.core.gui.Gui

/**
 * トロッコ挙動テスト用
 * @author Knit prg.
 */
class MinecartTestHandler : Listener {

    /**
     * プレイヤーがトロッコかボートに乗った瞬間に移動量を上書きする
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerMove(e: VehicleEnterEvent) {
        (e.entered as? Player)?.run{
            Gui.getInstance().error(this,"お前今、「乗った」よなァ...？")
            e.vehicle.velocity = e.vehicle.velocity.setX(5)
            //e.vehicle.velocity = e.vehicle.velocity.setY(5)
            Bukkit.getLogger().info(e.vehicle.velocity.toString())
        }
    }
}