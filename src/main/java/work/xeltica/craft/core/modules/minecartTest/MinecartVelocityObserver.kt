package work.xeltica.craft.core.modules.minecartTest

import org.bukkit.Bukkit
import org.bukkit.entity.Minecart
import org.bukkit.scheduler.BukkitRunnable

class MinecartVelocityObserver : BukkitRunnable() {
    override fun run() {
        Bukkit.getServer().onlinePlayers.forEach playersLoop@{ player ->
            val vehicle = player.vehicle ?: return
            if (vehicle !is Minecart) return
            Bukkit.getLogger().info("${vehicle.uniqueId} = ${vehicle.velocity}")
        }
    }
}