package work.xeltica.craft.core.modules.autoCrafter

import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.Material
import org.bukkit.block.Dispenser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AutoCrafterHandler : Listener {
    @EventHandler
    fun onDispenser(event: BlockPreDispenseEvent) {
        val block = event.block
        val blockData = block.blockData
        val state = block.state
        if (blockData.material != Material.DISPENSER) return
        val itemFrame = AutoCrafterModule.getNearbyGlowItemFrame(block) ?: return
        event.isCancelled = true
        val itemStack = itemFrame.item
        val recipes = AutoCrafterModule.getAllRecipesOf(itemStack)

        if (state is Dispenser) {
            AutoCrafterModule.autoCraft(state, recipes)
        }
    }
}