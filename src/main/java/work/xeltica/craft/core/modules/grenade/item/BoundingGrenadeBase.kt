package work.xeltica.craft.core.modules.grenade.item

import org.bukkit.entity.EntityType
import org.bukkit.entity.Snowball

abstract class BoundingGrenadeBase(protected var grenade: Snowball) : IGrenadeBase {
    open fun bound() {
        val velocity = grenade.velocity
        velocity.multiply(0.1)
        velocity.y *= -1
        val newGrenadeEntity = grenade.world.spawnEntity(grenade.location, EntityType.SNOWBALL) as Snowball
        newGrenadeEntity.velocity = velocity
        newGrenadeEntity.item = grenade.item
        grenade = newGrenadeEntity
    }

    override fun kill() {
        grenade.remove()
    }

    override fun hit() {
        bound()
    }

    fun getEntity(): Snowball {
        return grenade
    }
}