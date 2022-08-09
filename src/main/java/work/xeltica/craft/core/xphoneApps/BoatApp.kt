package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.WorldStore

/**
 * ボートを射出するアプリ。
 * @author Ebise Lutica
 */
class BoatApp : AppBase() {
    override fun getName(player: Player): String = "ボート召喚"

    override fun getIcon(player: Player): Material = Material.OAK_BOAT

    override fun onLaunch(player: Player) {
        player.performCommand("boat")
    }

    override fun isVisible(player: Player) = WorldStore.getInstance().canSummonVehicles(player.world)
}