package work.xeltica.craft.core.modules.item

import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.gui.Gui.Companion.getInstance

class ItemHandler : Listener {

    /**
     * カスタムアイテムのクラフト材料としての利用を禁止する
     */
    @EventHandler
    fun onGuardCraftingWithCustomItem(e: CraftItemEvent) {
        val hasLoreInMatrix = e.inventory.matrix
            .filterNotNull()
            .any { it.hasLore() }

        if (hasLoreInMatrix) {
            e.isCancelled = true
            val clicked = e.whoClicked
            if (clicked is Player) {
                getInstance().error(clicked, "§cカスタムアイテムをクラフティングに使用することはできません。")
            }
        }
    }

    @EventHandler
    fun onGuardUsingCustomItemAsFuelInSmelting(e: FurnaceBurnEvent) {
        if (e.fuel.hasLore()) {
            e.isCancelled = true
            e.block.world.playSound(e.block.location, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f)
        }
    }

    fun ItemStack.hasLore(): Boolean = (this.lore()?.size ?: 0) > 0
}