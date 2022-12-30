package work.xeltica.craft.core.modules.vehicle

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * ボートを射出するアプリ。
 * @author Ebise Lutica
 */
class BoatApp : AppBase() {
    override fun getName(player: Player): String = "ボート召喚"

    override fun getIcon(player: Player): Material = Material.OAK_BOAT

    override fun onLaunch(player: Player) {
        VehicleModule.trySummonBoat(player)
    }

    override fun isVisible(player: Player) = WorldModule.getWorldInfo(player.world).allowVehicleSpawn
}