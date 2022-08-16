package work.xeltica.craft.core.modules

import org.bukkit.Bukkit
import org.bukkit.entity.Boat
import org.bukkit.entity.Vehicle
import org.bukkit.entity.minecart.RideableMinecart
import work.xeltica.craft.core.api.Config
import java.io.IOException
import java.util.UUID
import java.util.logging.Logger

/**
 * サーバーに存在する乗り物を管理し、不要なものはデスポーンする処理などを行うストアです。
 * @author Xeltica
 */
object VehicleModule : ModuleBase() {
    override fun onEnable() {
        logger = Bukkit.getLogger()
        cm = Config("vehicles")
    }

    @JvmStatic
    fun registerVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) {
            return
        }
        val id = vehicle.uniqueId.toString()

        // 初期値を登録
        cm.conf[id] = 20 * 60 * 5
        try {
            cm.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun unregisterVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) {
            return
        }
        val id = vehicle.uniqueId.toString()

        // 削除
        unregisterVehicle(id)
    }

    // TODO: ワーカーとして書き直す
    @JvmStatic
    fun tick(tickCount: Int) {
        val c = cm.conf
        val ids = c.getKeys(false)
        for (id in ids) {
            var count = c.getInt(id)
            count -= tickCount
            c[id] = count
            if (count <= 0) {
                val e = Bukkit.getEntity(UUID.fromString(id))
                if (e == null) {
                    logger.warning("A vehicle ID:$id is not found on the server, so skipped to despawn.")
                } else {
                    e.remove()
                }
                unregisterVehicle(id)
            }
        }
        try {
            cm.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun isValidVehicle(v: Vehicle?): Boolean {
        return v is Boat || v is RideableMinecart
    }

    private fun unregisterVehicle(id: String) {
        // 削除
        cm.conf[id] = null
        try {
            cm.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private lateinit var cm: Config
    private lateinit var logger: Logger
}