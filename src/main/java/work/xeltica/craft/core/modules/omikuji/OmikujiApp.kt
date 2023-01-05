package work.xeltica.craft.core.modules.omikuji

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * おみくじアプリ
 * @author Lutica
 */
class OmikujiApp : AppBase() {
    override fun getName(player: Player): String = "おみくじ"

    override fun getIcon(player: Player): Material = Material.GOLD_INGOT

    override fun onLaunch(player: Player) {
        player.performCommand("omikuji")
    }
}
