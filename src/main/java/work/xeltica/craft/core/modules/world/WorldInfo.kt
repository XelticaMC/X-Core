package work.xeltica.craft.core.modules.world

import org.bukkit.Bukkit
import org.bukkit.World

data class WorldInfo(
    val name: String,
    val displayName: String,
    val isCitizenOnly: Boolean = false,
    val isStaffOnly: Boolean = false,
    val isCreativeWorld: Boolean = false,
    val canSleep: Boolean = false,
    val canEarnEbiPower: Boolean = false,
    val canRespawn: Boolean = true,
    val allowVehicleSpawn: Boolean = false,
    val allowAdvancements: Boolean = false,
    val allowRaids: Boolean = false,
    val respawnWorld: String = "",
    val description: String = "",
) {
    val world: World; get() = Bukkit.getWorld(name) ?: throw IllegalArgumentException(name)
}
