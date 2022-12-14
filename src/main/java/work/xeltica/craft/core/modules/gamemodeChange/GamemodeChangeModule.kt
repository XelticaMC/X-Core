package work.xeltica.craft.core.modules.gamemodeChange

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem

object GamemodeChangeModule : ModuleBase() {

    override fun onEnable() {
        Bukkit.getLogger().info("モジュールをローンチしてダーン")
    }

    private val gui by lazy { Gui.getInstance() }
    private val app by lazy { GamemodeChangeApp() }

    private fun getGamemodeList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("サバイバル", { app.selectMenuItem("survival" , player)}, Material.ZOMBIE_HEAD),
                MenuItem("クリエイティブ", { app.selectMenuItem("creative" , player)}, Material.SKELETON_SKULL),
                MenuItem("アドベンチャー", { app.selectMenuItem("adventure" , player)}, Material.CREEPER_HEAD),
                MenuItem("スペクテイター", { app.selectMenuItem("spectator" , player)}, Material.PLAYER_HEAD),
        )
    }

    fun start(player: Player) {
        gui.openMenu(player, "変更するゲームモードを選択してください", getGamemodeList(player))
    }

}