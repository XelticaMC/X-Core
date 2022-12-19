package work.xeltica.craft.core.modules.vehicle

import org.bukkit.Bukkit
import org.bukkit.entity.Boat
import org.bukkit.entity.Vehicle
import org.bukkit.entity.minecart.RideableMinecart
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.*

object VehicleModule: ModuleBase() {
    private lateinit var config: Config
    private val logger = Bukkit.getLogger()

    override fun onEnable() {
        config = Config("vehicles")
    }

    // TODO("ワーカーとして書き直す")
    fun tick(tickCount: Int) {
        val conf = config.conf
        val ids = conf.getKeys(false)
        for (id in ids) {
            var tick = conf.getInt(id)
            tick -= tickCount
            conf.set(id, tick)
            if (tick <= 0) {
                val entity = Bukkit.getEntity(UUID.fromString(id))
                if (entity == null) {
                    logger.warning("A vehicle ID:$id is not found on the server, so skipped to despawn.")
                } else {
                    entity.remove()
                }
                unregisterVehicle(id)
            }
        }
    }

    fun registerVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        //初期値を登録
        config.conf.set(id, 20 * 60 * 5)
        saveTask()
    }

    fun unregisterVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        unregisterVehicle(id)
    }

    fun isValidVehicle(v: Vehicle?): Boolean {
        return v is Boat || v is RideableMinecart
    }

    private fun unregisterVehicle(id: String) {
        config.conf.set(id, null)
        saveTask()
    }

    private fun saveTask() {
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}