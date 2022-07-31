package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * イベント用：初期スポーンに戻るアプリ
 */
class EventRespawnApp : AppBase() {
    override fun getName(player: Player): String = "初期スポーンに移動"

    override fun getIcon(player: Player): Material = Material.FIREWORK_ROCKET

    override fun onLaunch(player: Player) {
        player.performCommand("respawn")
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event"
    }
}