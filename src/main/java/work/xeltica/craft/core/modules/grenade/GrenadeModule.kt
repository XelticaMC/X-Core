package work.xeltica.craft.core.modules.grenade

import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Snowball
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.grenade.item.FragGrenade
import work.xeltica.craft.core.modules.grenade.item.IGrenadeBase
import work.xeltica.craft.core.modules.grenade.item.Molotov
import java.util.UUID

object GrenadeModule : ModuleBase() {
    private val grenadeEntity = HashMap<UUID, IGrenadeBase>()

    override fun onEnable() {
        registerHandler(GrenadeHandler())
    }

    fun getGrenadeEntity(entity: ThrowableProjectile): IGrenadeBase? {
        return grenadeEntity[entity.uniqueId]
    }

    fun registerGrenadeEntity(entity: ThrowableProjectile): IGrenadeBase? {
        if (!isGrenadeItem(entity.item)) return null
        val lore = entity.item.lore() ?: return null
        val label = lore[1] as? TextComponent ?: return null
        when (label.content()) {
            FragGrenade.name -> {
                if (entity is Snowball) {
                    grenadeEntity[entity.uniqueId] = FragGrenade(entity)
                }
            }
            Molotov.name -> {
                if (entity is ThrownPotion) {
                    grenadeEntity[entity.uniqueId] = Molotov(entity)
                }
            }
        }
        return grenadeEntity[entity.uniqueId]
    }

    fun destroyGrenadeEntity(entityId: UUID) {
        grenadeEntity.remove(entityId)
    }

    fun replaceGrenadeEntityUUID(oldUUID: UUID, newUUID: UUID) {
        val grenade = grenadeEntity[oldUUID] ?: return
        grenadeEntity.remove(oldUUID)
        grenadeEntity[newUUID] = grenade
    }

    fun isGrenadeEntity(entity: ThrowableProjectile): Boolean {
        return grenadeEntity.containsKey(entity.uniqueId)
    }

    fun isGrenadeItem(item: ItemStack): Boolean {
        val lore = item.lore() ?: return false
        val label = lore[0] as? TextComponent ?: return false
        if (label.content() == "グレネード") return true
        return false
    }

    fun createGrenadeItem(type: GrenadeType): ItemStack {
        return when (type) {
            GrenadeType.FRAG_GRENADE -> FragGrenade.createItem()
            GrenadeType.MOLOTOV -> Molotov.createItem()
        }
    }

    enum class GrenadeType {
        FRAG_GRENADE,
        MOLOTOV,
    }

}