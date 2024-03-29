package work.xeltica.craft.core.modules.vehicle

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * トロッコを射出するアプリ。
 * @author Lutica
 */
class CartApp : AppBase() {
    override fun getName(player: Player): String = "トロッコ召喚"

    override fun getIcon(player: Player): Material = Material.MINECART

    override fun onLaunch(player: Player) {
        VehicleModule.trySummonCart(player)
    }

    override fun isVisible(player: Player) = WorldModule.getWorldInfo(player.world).allowVehicleSpawn
}

