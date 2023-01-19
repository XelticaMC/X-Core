package work.xeltica.craft.core.modules.xphone

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * サイドバー切り替えアプリ
 * @author Lutica
 */
class SidebarApp : AppBase() {
    override fun getName(player: Player): String = "サイドバー切り替え"

    override fun getIcon(player: Player): Material = Material.FILLED_MAP

    override fun onLaunch(player: Player) {
        player.performCommand("sb toggle")
    }
}

