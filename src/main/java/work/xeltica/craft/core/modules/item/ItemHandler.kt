package work.xeltica.craft.core.modules.item

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.modules.item.ItemModule.isCustomItem

class ItemHandler : Listener {

    /**
     * カスタムアイテムのクラフト材料としての利用を禁止する
     */
    @EventHandler
    fun onGuardCraftingWithCustomItem(e: CraftItemEvent) {
        val hasLoreInMatrix = e.inventory.matrix
            .filterNotNull()
            .any { it.isCustomItem() }

        if (hasLoreInMatrix) {
            e.isCancelled = true
            val clicked = e.whoClicked
            if (clicked is Player) {
                getInstance().error(clicked, "${ChatColor.RED}カスタムアイテムをクラフティングに使用することはできません。")
            }
        }
    }

    @EventHandler
    fun onGuardUsingCustomItemAsFuelInSmelting(e: FurnaceBurnEvent) {
        if (e.fuel.isCustomItem()) {
            e.isCancelled = true
            e.block.world.playSound(e.block.location, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f)
        }
    }
}