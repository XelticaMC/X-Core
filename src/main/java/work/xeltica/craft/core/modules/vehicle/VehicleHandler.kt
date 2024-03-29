package work.xeltica.craft.core.modules.vehicle

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleCreateEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule.hasAchieved
import work.xeltica.craft.core.modules.world.WorldModule

class VehicleHandler : Listener {

    @EventHandler
    fun onEnter(e: VehicleEnterEvent) {
        VehicleModule.unregisterVehicle(e.vehicle)
    }

    @EventHandler
    fun onExit(e: VehicleExitEvent) {
        VehicleModule.registerVehicle(e.vehicle)
    }

    @EventHandler
    fun onCreated(e: VehicleCreateEvent) {
        VehicleModule.registerVehicle(e.vehicle)
    }

    @EventHandler
    fun onDestroyed(e: VehicleDestroyEvent) {
        val vehicle = e.vehicle
        if (VehicleModule.isValidVehicle(vehicle)) {
            e.isCancelled = true
            vehicle.remove()
        }
        VehicleModule.unregisterVehicle(vehicle)
    }

    @EventHandler
    fun onVehicleDestroyed(e: EntityDeathEvent) {
        if (e.entityType == EntityType.MINECART || e.entityType == EntityType.BOAT) {
            e.drops.clear()
        }
    }

    @EventHandler
    fun onPlayerSpawnVehicle(e: PlayerInteractEvent) {
        if (WorldModule.getWorldInfo(e.player.world).isCreativeWorld) return

        val block = e.clickedBlock ?: return
        if (VehicleModule.vehicleItems.contains(e.material)) {
            val loc = block.location.add(e.blockFace.direction)
            loc.world.spawnParticle(Particle.ASH, loc, 8, 1.0, 1.0, 1.0)
            loc.world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f)
            val isCart = e.material == Material.MINECART
            if (!hasAchieved(e.player, if (isCart) Hint.MINECART else Hint.BOAT)) {
                e.player.sendMessage(
                    "${ChatColor.GREEN}" + (if (isCart) "トロッコ" else "ボート") + "${ChatColor.RESET}は${ChatColor.AQUA}X Phone${ChatColor.RESET}を用いてどこでも召喚できます。" +
                            "X Phoneをお持ちでなければ${ChatColor.GREEN}/phone${ChatColor.RESET}コマンドで入手できます。"
                )
            }
            e.isCancelled = true
        }
    }
}