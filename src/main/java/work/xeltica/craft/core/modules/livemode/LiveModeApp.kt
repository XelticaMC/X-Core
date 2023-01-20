package work.xeltica.craft.core.modules.livemode

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * 配信モード切り替えアプリ
 * @author Lutica
 */
class LiveModeApp : AppBase() {
    override fun getName(player: Player): String = "配信モードを${if (isLiveMode(player)) "オフ" else "オン"}にする"

    override fun getIcon(player: Player): Material = Material.RED_DYE

    override fun onLaunch(player: Player) {
        LiveModeModule.setLiveMode(player, !isLiveMode(player))
    }

    private fun isLiveMode(player: Player) = LiveModeModule.isLiveMode(player)
}