package work.xeltica.craft.core.modules.minecartTest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import work.xeltica.craft.core.gui.Gui

/**
 * トロッコ挙動テスト用
 * 以下メモ:
 *  トロッコ内のプレイヤーの移動(上下左右、ジャンプ、視点移動)は検知できない、なんで？
 *  トロッコの移動量は2~4程度で頭打ちになる傾向がある(しかもsetMaxSpeedで設定した通りの値が上限になる訳でもないらしい...？)
 * @author Knit prg.
 */
class MinecartTestHandler : Listener {

    /**
     * プレイヤーがトロッコかボートに乗った瞬間に移動量を上書きする
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerEnter(e: VehicleEnterEvent) {
        (e.entered as? Player)?.run {
            Gui.getInstance().error(this, "お前今、「乗った」よなァ...？")
            e.vehicle.velocity = e.vehicle.velocity.setX(0.5)
            Bukkit.getLogger().info("A player is now on minecart: new X velocity of ${e.vehicle.uniqueId} set to 0.25")
            //e.vehicle.velocity = e.vehicle.velocity.setY(5)
        }
    }

    /**
     * トロッコに乗ったプレイヤーが的に矢を当てた際にトロッコを加速させる
     * 仮で正のX方向に移動している前提のハードコーディングをしている点に注意
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onArrowHit(e: ProjectileHitEvent) {
        //的でない場合終了
        if (e.hitBlock?.type != Material.TARGET) return
        //打たれた物体が矢ではない場合終了
        if (e.entity !is AbstractArrow) return
        val shooter = e.entity.shooter
        //プレイヤーによって打たれた矢ではない場合終了
        if (shooter !is Player) return
        //プレイヤーが乗り物に乗っていない場合終了
        val vehicle = shooter.vehicle ?: return
        //プレイヤーが載っている乗り物がトロッコでない場合終了
        if (vehicle !is Minecart) return
        val newVelocity = vehicle.velocity.x + 10
        vehicle.velocity = vehicle.velocity.setX(newVelocity)
        Bukkit.getLogger().info("Arrow shot by a player hits to a target: new X velocity of ${vehicle.uniqueId} is $newVelocity")
    }

    /**
     * トロッコに乗ったプレイヤーが左クリックをした場合にトロッコを加速、右クリックをした場合に減速させる
     * 仮で正のX方向に移動している前提のハードコーディングをしている点に注意
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerClick(e: PlayerInteractEvent) {
        //プレイヤーがトロッコに乗っていない場合終了
        val vehicle = e.player.vehicle ?: return
        if (listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK).contains(e.action)) {
            val newVelocity = vehicle.velocity.x + 1
            vehicle.velocity = vehicle.velocity.setX(newVelocity)
            Bukkit.getLogger().info("Player left-clicked: new X velocity of ${vehicle.uniqueId} is $newVelocity")
        } else {
            var newVelocity = vehicle.velocity.x - 0.25
            if (newVelocity < 0) newVelocity = 0.0
            vehicle.velocity = vehicle.velocity.setX(newVelocity)
            Bukkit.getLogger().info("Player right-clicked: new X velocity of ${vehicle.uniqueId} is $newVelocity")
        }
    }
}