package work.xeltica.craft.core.modules.stamprally

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.MemorySection

class Stamp(val name: String, val loc: Location) {

    fun serialize(): Map<String, Any> {
        val data = HashMap<String, Any>()
        data["name"] = name
        data["world"] = loc.world.name
        data["x"] = loc.x
        data["y"] = loc.y
        data["z"] = loc.z
        return data
    }

    companion object {
        fun deserialize(data: MemorySection): Stamp {
            val name = data["name"] as String
            val world = data["world"] as String
            val x = data["x"] as Double
            val y = data["y"] as Double
            val z = data["z"] as Double
            val loc = Location(Bukkit.getWorld(world), x, y, z)
            return Stamp(name, loc)
        }
    }
}