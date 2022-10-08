package work.xeltica.craft.core.modules.halloween

import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import work.xeltica.craft.core.events.EntityMobBallHitEvent
import work.xeltica.craft.core.modules.halloween.HalloweenModule.isEventMob

class HalloweenHandler : Listener {
    @EventHandler
    fun onMobSpawnInMainWorld(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity !is Monster) return
        val world = entity.world
        if (world.name != "event2") return
        if (entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return
        HalloweenModule.replaceMob(entity)
    }

    @EventHandler
    fun onEventMobDamaged(e: EntityDamageEvent) {
        // イベントモブでなければ対象外
        if (!e.entity.isEventMob()) return
        // プレイヤーに傷つけられた場合は即死
        if (e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e is EntityDamageByEntityEvent && e.damager.type == EntityType.PLAYER) {
            e.damage = 99999.0
            return
        }
        e.isCancelled = true
    }

    @EventHandler
    fun onEventMobDeath(e: EntityDeathEvent) {
        // イベントモブでなければ対象外
        if (!e.entity.isEventMob()) return
        val killer = e.entity.killer
        if (killer == null) {
            Bukkit.getLogger()
                .warning("Event Mob must be killed by player to death, but it died by ${e.entity.lastDamageCause?.cause ?: "(unknown)"}")
            return
        }
        HalloweenModule.replaceDrops(e.drops, killer)
    }

    @EventHandler
    fun onEventMobHitMobBall(e: EntityMobBallHitEvent) {
        if (e.target.isEventMob()) {
            e.isCancelled = true
        }
    }
}