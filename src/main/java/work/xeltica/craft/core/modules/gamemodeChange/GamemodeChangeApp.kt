package work.xeltica.craft.core.modules.gamemodeChange

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.xphone.AppBase

class GamemodeChangeApp : AppBase() {

    private val gui by lazy { Gui.getInstance() }
    private val app by lazy { GamemodeChangeApp() }

    private fun getGameModeList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("サバイバル", { app.selectMenuItem(GameMode.SURVIVAL, player) }, Material.ZOMBIE_HEAD),
                MenuItem("クリエイティブ", { app.selectMenuItem(GameMode.CREATIVE, player) }, Material.SKELETON_SKULL),
                MenuItem("アドベンチャー", { app.selectMenuItem(GameMode.ADVENTURE, player) }, Material.CREEPER_HEAD),
                MenuItem("スペクテイター", { app.selectMenuItem(GameMode.SPECTATOR, player) }, Material.PLAYER_HEAD),
        )
    }

    override fun getName(player: Player): String {
        return "ゲームモード変更アプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.PLAYER_HEAD
    }

    override fun onLaunch(player: Player) {
        gui.openMenu(player, "変更するゲームモードを選択してください", getGameModeList(player))
    }

    private fun selectMenuItem(gameMode: GameMode, player: Player) {
        player.gameMode = gameMode
    }

//    override fun isVisible(player: Player): Boolean {
//        return player.hasPermission("otanoshimi.command.report")
//    }


}