package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.WorldManagementModule

/**
 * イベント用：メインワールドに戻るアプリ
 */
class EventReturnWorldApp : AppBase() {
    override fun getName(player: Player): String = "メインワールドへ帰る"

    override fun getIcon(player: Player): Material = Material.GRASS_BLOCK

    override fun onLaunch(player: Player) {
        WorldManagementModule.teleportToSavedLocation(player, "main")
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event"
    }
}