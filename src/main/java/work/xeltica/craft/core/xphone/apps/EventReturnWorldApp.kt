package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.world.WorldModule

/**
 * イベント用：メインワールドに戻るアプリ
 */
class EventReturnWorldApp : AppBase() {
    override fun getName(player: Player): String = "メインワールドへ帰る"

    override fun getIcon(player: Player): Material = Material.GRASS_BLOCK

    override fun onLaunch(player: Player) {
        WorldModule.teleportToSavedLocation(player, "main")
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event" || player.world.name == "event2"
    }
}