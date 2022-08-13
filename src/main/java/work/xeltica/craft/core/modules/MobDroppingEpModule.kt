package work.xeltica.craft.core.modules

import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import java.util.Locale
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config

object MobDroppingEpModule : ModuleBase() {

    override fun onEnable() {
        XCorePlugin.instance.saveResource("mobEP.yml", false)
        config = Config("mobEP")
    }

    @JvmStatic
    fun getMobDropEP(entity: Entity, event: EntityDeathEvent): Int {
        val conf = config.conf
        if (entity.type == EntityType.ENDERMAN) {
            if (event.drops.stream().map { obj: ItemStack -> obj.type }.toList().contains(Material.ENDER_PEARL)) {
                return conf.getInt("pearl_enderman")
            }
        }
        if (entity.type == EntityType.CREEPER) {
            if ((entity as Creeper).isPowered) {
                return conf.getInt("charged_creeper")
            }
        }
        if (conf.contains(entity.type.name.lowercase(Locale.getDefault()))) {
            return conf.getInt(entity.type.name.lowercase(Locale.getDefault()))
        }
        return if (entity is Monster) {
            conf.getInt("other_enemy")
        } else {
            conf.getInt("friendly_mob")
        }
    }

    private lateinit var config: Config
}