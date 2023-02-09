package work.xeltica.craft.core.modules.gamemodeChange

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.xphone.AppBase

class GamemodeChangeApp : AppBase() {

    private val gui by lazy { Gui.getInstance() }

    override fun getName(player: Player): String {
        return "ゲームモード変更アプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.PLAYER_HEAD
    }

    override fun onLaunch(player: Player) {
        gui.openMenu(
            player,
            "変更するゲームモードを選択してください",
            listOf(
                MenuItem("サバイバル", { player.gameMode = GameMode.SURVIVAL }, Material.ZOMBIE_HEAD),
                MenuItem("クリエイティブ", { player.gameMode = GameMode.CREATIVE }, Material.SKELETON_SKULL),
                MenuItem("アドベンチャー", { player.gameMode = GameMode.ADVENTURE }, Material.CREEPER_HEAD),
                MenuItem("スペクテイター", { player.gameMode = GameMode.SPECTATOR }, Material.PLAYER_HEAD),
            )
        )
    }

    //    override fun isVisible(player: Player): Boolean {
    //        return player.hasPermission("otanoshimi.command.report")
    //    }


}