package work.xeltica.craft.core.modules.vehicle

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class VehicleObserver : BukkitRunnable() {
    override fun run() {
        for (id in VehicleModule.getVehicleIds()) {
            var tick = VehicleModule.getTick(id)
            VehicleModule.setTick(id, tick--)
            if (tick <= 0) {
                val entity = Bukkit.getEntity(UUID.fromString(id))
                if (entity == null) {
                    Bukkit.getLogger().warning("乗り物 ID:$id はサーバーに見つからなかったため、デスポーン処理をスキップします。")
                } else {
                    entity.remove()
                }
                VehicleModule.unregisterVehicle(id)
            }
        }
    }
}