package work.xeltica.craft.core.modules.gamemodeChange

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class GamemodeChangeApp : AppBase() {
    override fun getName(player: Player): String {
        return "ゲームモード変更アプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.PLAYER_HEAD
    }

    override fun onLaunch(player: Player) {
        GamemodeChangeModule.start(player)
    }

    fun selectMenuItem(commandString: String, player: Player){
        player.performCommand("gamemode $commandString")
    }

//    override fun isVisible(player: Player): Boolean {
//        return player.hasPermission("otanoshimi.command.report")
//    }


}