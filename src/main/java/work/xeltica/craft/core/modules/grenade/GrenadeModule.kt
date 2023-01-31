package work.xeltica.craft.core.modules.grenade

import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.grenade.item.FragGrenade
import work.xeltica.craft.core.modules.grenade.item.Molotov

object GrenadeModule : ModuleBase() {

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