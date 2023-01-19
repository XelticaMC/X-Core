package work.xeltica.craft.core.modules.autoCrafter

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.utils.CollectionHelper.sum

/**
 * 自動クラフトのために用いる作業台レシピ。
 */
@JvmRecord
data class CraftRecipe(val ingredients: List<ItemStack>, val result: ItemStack) {
    val fixedRecipe: Map<Material, Int>
        get() = ingredients.groupingBy { it.type }.sum { it.amount }
}
