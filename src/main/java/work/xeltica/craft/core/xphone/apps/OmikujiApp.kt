package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * おみくじアプリ
 * @author Ebise Lutica
 */
class OmikujiApp : AppBase() {
    override fun getName(player: Player): String = "おみくじ"

    override fun getIcon(player: Player): Material = Material.GOLD_INGOT

    override fun onLaunch(player: Player) {
        player.performCommand("omikuji")
    }
}
