package work.xeltica.craft.core.modules.hub

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerTeleportEvent

class HubHandler: Listener {
    @EventHandler
    fun onPlayerHurt(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (playerIsInHub(player)) {
            e.isCancelled = true
            if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                val loc = player.world.spawnLocation
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        }
    }

    @EventHandler
    fun onPlayerHunger(e: FoodLevelChangeEvent) {
        val player = e.entity
        if (playerIsInHub(player)) {
            player.foodLevel = 20
            e.isCancelled = true
        }
    }

    private fun playerIsInHub(p: Entity): Boolean {
        return p.world.name.equals("hub2", ignoreCase = true)
    }
}