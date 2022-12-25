package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.world.WorldModule

/**
 * トロッコを射出するアプリ。
 * @author Ebise Lutica
 */
class CartApp : AppBase() {
    override fun getName(player: Player): String = "トロッコ召喚"

    override fun getIcon(player: Player): Material = Material.MINECART

    override fun onLaunch(player: Player) {
        player.performCommand("cart")
    }

    override fun isVisible(player: Player) = WorldModule.canSummonVehicles(player.world)
}

