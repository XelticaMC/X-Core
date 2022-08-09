package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.PlayerStore

/**
 * 配信モード切り替えアプリ
 * @author Ebise Lutica
 */
class LiveModeApp : AppBase() {
    override fun getName(player: Player): String = "配信モードを${if (isLiveMode(player)) "オフ" else "オン"}にする"

    override fun getIcon(player: Player): Material = Material.RED_DYE

    override fun onLaunch(player: Player) {
        PlayerStore.getInstance().setLiveMode(player, !isLiveMode(player))
    }

    private fun isLiveMode(player: Player) = PlayerStore.getInstance().isLiveMode(player)
}