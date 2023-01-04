package work.xeltica.craft.core.modules.hint

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * ヒントアプリ
 * @author Lutica
 */
class HintApp : AppBase() {
    override fun getName(player: Player): String = "ヒント"

    override fun getIcon(player: Player): Material = Material.LIGHT

    override fun onLaunch(player: Player) {
        player.performCommand("hint")
    }
}
