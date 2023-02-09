package work.xeltica.craft.core.modules.grenade.item

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin

class FragGrenade(grenade: Snowball) : BoundingGrenadeBase(grenade) {
    override fun getName(): String {
        return name
    }

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

    companion object {
        const val name = "フラググレネード"
        fun createItem(): ItemStack {
            val item = ItemStack(Material.SNOWBALL)
            item.editMeta {
                it.displayName(Component.text(name))
                it.lore(listOf(Component.text("グレネード"), Component.text(name)))
            }
            return item
        }
    }
}