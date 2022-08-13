package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * 処罰を実行するアプリ
 * @author Ebise Lutica
 */
class PunishApp : AppBase() {
    override fun getName(player: Player): String = "（スタッフ用）処罰"

    override fun getIcon(player: Player): Material = Material.NETHER_STAR

    override fun onLaunch(player: Player) {
        player.performCommand("report")
    }

    override fun isVisible(player: Player): Boolean {
        return player.hasPermission("otanoshimi.command.report")
    }
}
