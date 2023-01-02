package work.xeltica.craft.core.modules.autoCrafter

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.Dispenser
import org.bukkit.entity.GlowItemFrame
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.material.Directional
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.item.ItemModule.isCustomItem
import work.xeltica.craft.core.utils.CollectionHelper.sum

object AutoCrafterModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(AutoCrafterHandler())
    }

    fun getNearbyGlowItemFrame(block: Block): GlowItemFrame? {
        val frames = block.location.toCenterLocation().getNearbyEntitiesByType(GlowItemFrame::class.java, 1.0)

        return frames.firstOrNull {
            it.location.clone()
                .add(it.attachedFace.direction).block.location.toBlockLocation() == block.location.toBlockLocation()
        }
    }

    fun getAllRecipesOf(result: ItemStack): List<CraftRecipe> {
        return Bukkit.getRecipesFor(result).mapNotNull {
            when (it) {
                is ShapedRecipe -> {
                    val ingredients = it.ingredientMap.values
                        .filterNotNull()
                        .toList()
                    CraftRecipe(ingredients, it.result)
                }

                is ShapelessRecipe -> {
                    val ingredients = it.ingredientList
                        .filterNotNull()
                        .toList()
                    CraftRecipe(ingredients, it.result)
                }

                else -> null
            }
        }
    }

    fun autoCraft(dispenser: Dispenser, recipes: List<CraftRecipe>) {
        val blockData = dispenser.blockData
        val inventory = dispenser.inventory
        val contents = inventory.contents.filterNotNull()

        if (contents.any { it.isCustomItem() }) {
            dispenser.world.playSound(dispenser.location, Sound.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1f, 1f)
            return
        }

        for (recipe in recipes) {
            val fixedInventory = contents.groupingBy { it.type }.sum { it.amount }.toMutableMap()
            var flag = true
            for (ingredient in recipe.fixedRecipe.keys) {
                if (fixedInventory.containsKey(ingredient) && fixedInventory[ingredient]!! >= recipe.fixedRecipe[ingredient]!!) {
                    fixedInventory.replace(
                        ingredient,
                        fixedInventory[ingredient]!! - recipe.fixedRecipe[ingredient]!!
                    )
                    continue
                }
                flag = false
            }

            if (flag) {
                for (itemStack in contents) {
                    inventory.remove(itemStack)
                }
                for ((key, value) in fixedInventory) {
                    val itemStack = ItemStack(key)
                    itemStack.amount = value
                    inventory.addItem(itemStack)
                }
                if (blockData is Directional) {
                    val location: Location = dispenser.location.toBlockLocation().add(blockData.facing.direction)
                    val result: HashMap<Int, ItemStack> = dispenser.inventory.addItem(recipe.result)
                    if (result.isNotEmpty()) {
                        for (i in result.values) {
                            dispenser.world.dropItem(location, i)
                        }
                    }
                    return
                }
            }
        }
    }
}