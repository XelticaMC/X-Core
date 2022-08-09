package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * ヒントアプリ
 * @author Ebise Lutica
 */
class HintApp : AppBase() {
    override fun getName(player: Player): String = "ヒント"

    override fun getIcon(player: Player): Material = Material.LIGHT

    override fun onLaunch(player: Player) {
        player.performCommand("hint")
    }
}
