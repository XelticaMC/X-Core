package work.xeltica.craft.core.modules.grenade.item

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin

class StunGrenade(grenade: Snowball) : BoundingGrenadeBase(grenade) {
    private val radius = 5.0
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
        for (entity in grenade.getNearbyEntities(radius, radius, radius)) {
            if (entity is Player) {
                entity.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 200, 10))
            }
        }
    }

    companion object {
        const val name = "スタングレネード"
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