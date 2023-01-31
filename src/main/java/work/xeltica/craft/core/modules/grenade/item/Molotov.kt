package work.xeltica.craft.core.modules.grenade.item

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import kotlin.math.absoluteValue

class Molotov(val grenade: ThrownPotion) : IGrenadeBase {
    override fun getName(): String {
        return name
    }

    override fun hit() {
    }

    override fun throwGrenade() {
    }

    override fun explode() {
        val center = grenade.location
        for (i in -2..2) {
            for (j in -2..2) {
                if (i.absoluteValue == 2 && i.absoluteValue == 2) continue
                var firePos = center.add(i.toDouble(), 0.0, j.toDouble())
                if (firePos.add(0.0, -1.0, 0.0).block.isEmpty) {
                    firePos = firePos.add(0.0, -1.0, 0.0)
                }
                if (firePos.block.isEmpty) {
                    if (!firePos.add(0.0, -1.0, 0.0).block.isEmpty) {
                        firePos.block.type = Material.FIRE
                    }
                }
            }
        }
    }

    override fun kill() {
    }

    companion object {
        const val name = "火炎瓶"
        fun createItem(): ItemStack {
            val item = ItemStack(Material.SPLASH_POTION)
            item.editMeta {
                if (it is PotionMeta) {
                    it.displayName(Component.text(name))
                    it.color = Color.RED
                    it.lore(listOf(Component.text("グレネード"), Component.text(name)))
                }
            }
            return item
        }
    }
}