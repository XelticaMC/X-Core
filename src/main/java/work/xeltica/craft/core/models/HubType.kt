package work.xeltica.craft.core.models

import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * ロビーの種類を定義しています。
 * @author Xeltica
 */
enum class HubType(var worldName: String) {
    // メインロビー
    Main("hub2"),  // メインロビー(新規)
    NewComer("hub2", Vector3(293, 11, 63));

    constructor(worldName: String, vec: Vector3) : this(worldName) {
        location = vec
    }

    val x: Int
        get() = location!!.x
    val y: Int
        get() = location!!.y
    val z: Int
        get() = location!!.z
    val spigotLocation: Location
        get() = Location(Bukkit.getWorld(worldName), x + .5, y + .5, z + .5)
    var location: Vector3? = null

}