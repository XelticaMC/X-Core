package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * サイドバー切り替えアプリ
 * @author Ebise Lutica
 */
class SidebarApp : AppBase() {
    override fun getName(player: Player): String = "サイドバー切り替え"

    override fun getIcon(player: Player): Material = Material.FILLED_MAP

    override fun onLaunch(player: Player) {
        player.performCommand("sb toggle")
    }
}

