package work.xeltica.craft.core.modules.xphone

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * サーバー投票アプリ
 * @author Lutica
 */
class VoteApp : AppBase() {
    override fun getName(player: Player): String = "サーバー投票"

    override fun getIcon(player: Player): Material = Material.NAME_TAG

    override fun onLaunch(player: Player) {
        player.performCommand("vote")
    }

    override fun isVisible(player: Player): Boolean = !XphoneModule.isBedrockPlayer(player) && player.world.name != "event"
}

