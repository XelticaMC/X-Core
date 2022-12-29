package work.xeltica.craft.core.modules.vehicle

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * トロッコを射出するアプリ。
 * @author Ebise Lutica
 */
class CartApp : AppBase() {
    override fun getName(player: Player): String = "トロッコ召喚"

    override fun getIcon(player: Player): Material = Material.MINECART

    override fun onLaunch(player: Player) {
        VehicleModule.trySummonCart(player)
    }

    override fun isVisible(player: Player) = WorldModule.canSummonVehicles(player.world)
}

