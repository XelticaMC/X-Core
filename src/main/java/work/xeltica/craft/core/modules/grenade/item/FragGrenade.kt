package work.xeltica.craft.core.modules.grenade.item

import org.bukkit.entity.Snowball
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin

class FragGrenade(grenade: Snowball) : BoundingGrenadeBase(grenade) {
    override val name: String = "フラググレネード"

    override fun throwGrenade() {
        object : BukkitRunnable() {
            override fun run() {
                explode()
                kill()
            }
        }.runTaskLater(XCorePlugin.instance, 40)
    }

    override fun explode() {
        grenade.world.createExplosion(grenade.location, 2f, false, false)
    }
}