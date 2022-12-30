package work.xeltica.craft.core.models

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.utils.CollectionHelper.sum

@JvmRecord
data class CraftRecipe(val ingredients: ArrayList<ItemStack>, val result: ItemStack) {
    val fixedRecipe: Map<Material, Int>
        get() = ingredients.groupingBy { it.type }.sum { it.amount }
}