package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * 市民かどうかを確認するアプリ
 * @author Ebise Lutica
 */
class PromoApp : AppBase() {
    override fun getName(player: Player): String = "市民システム"

    override fun getIcon(player: Player): Material = Material.NETHER_STAR

    override fun onLaunch(player: Player) {
        player.performCommand("promo")
    }
}
