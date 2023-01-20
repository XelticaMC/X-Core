package work.xeltica.craft.core.modules.hub

import org.bukkit.Bukkit
import org.bukkit.Location
import work.xeltica.craft.core.math.Vector3

enum class HubType(val worldName: String, val location: Vector3? = null) {
    /**
     * 通常ロビー
     */
    Main("hub2"),

    /**
     * 新規さん用ロビー
     */
    NewComer("hub2", Vector3(293, 11, 63));

    fun getSpigotLocation(): Location {
        return Location(Bukkit.getWorld(worldName), location!!.x + .5, location.y + .5, location.z + .5)
    }
}