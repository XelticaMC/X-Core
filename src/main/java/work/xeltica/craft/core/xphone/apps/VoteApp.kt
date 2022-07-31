package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.XphoneOs

/**
 * サーバー投票アプリ
 * @author Ebise Lutica
 */
class VoteApp : AppBase() {
    override fun getName(player: Player): String = "サーバー投票"

    override fun getIcon(player: Player): Material = Material.NAME_TAG

    override fun onLaunch(player: Player) {
        player.performCommand("vote")
    }

    override fun isVisible(player: Player): Boolean = !XphoneOs.isBedrockPlayer(player) &&  player.world.name !== "event"
}

