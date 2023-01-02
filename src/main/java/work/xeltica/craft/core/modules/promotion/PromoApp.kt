package work.xeltica.craft.core.modules.promotion

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

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
