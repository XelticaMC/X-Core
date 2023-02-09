package work.xeltica.craft.core.modules.grenade

import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent

class GrenadeHandler : Listener {
    @EventHandler
    fun onThrow(event: ProjectileLaunchEvent) {
        val entity = event.entity
        if (entity is ThrowableProjectile) {
            GrenadeModule.registerGrenadeEntity(entity)?.throwGrenade()
        }
    }

    @EventHandler
    fun onSplash(event: PotionSplashEvent) {
        event.isCancelled = true
        if (GrenadeModule.isGrenadeEntity(event.entity)) {
            GrenadeModule.getGrenadeEntity(event.entity)?.explode()
        }
    }

    @EventHandler
    fun onHit(event: ProjectileHitEvent) {
        event.isCancelled = true
        val entity = event.entity
        if (entity !is ThrowableProjectile) return
        if (GrenadeModule.isGrenadeEntity(entity)) {
            GrenadeModule.getGrenadeEntity(entity)?.hit()
        }
    }
}