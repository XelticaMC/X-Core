package work.xeltica.craft.core.modules.grenade.item

import org.bukkit.Material
import org.bukkit.entity.ThrownPotion
import kotlin.math.absoluteValue

class Molotov(val grenade: ThrownPotion) : IGrenadeBase {
    override val name: String = "火炎瓶"
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
}