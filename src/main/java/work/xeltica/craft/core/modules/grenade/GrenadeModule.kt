package work.xeltica.craft.core.modules.grenade

import net.kyori.adventure.text.TextComponent
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.grenade.item.FragGrenade
import work.xeltica.craft.core.modules.grenade.item.Molotov

object GrenadeModule : ModuleBase() {


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