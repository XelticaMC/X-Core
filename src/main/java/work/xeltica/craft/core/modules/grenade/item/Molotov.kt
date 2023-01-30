package work.xeltica.craft.core.modules.grenade.item

import org.geysermc.connector.entity.ThrownPotionEntity

class Molotov(val grenade: ThrownPotionEntity) : IGrenadeBase {
    override val name: String = "火炎瓶"
    override fun throwGrenade() {
    }

    override fun explode() {
        val center = grenade.position
        
    }

    override fun kill() {
    }
}