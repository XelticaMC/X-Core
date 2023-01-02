package work.xeltica.craft.core.modules.omikuji

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.events.NewMorningEvent

class OmikujiHandler : Listener {
    @EventHandler
    fun onNewMorning(e: NewMorningEvent) {
        OmikujiModule.reset()
    }

    @EventHandler
    fun onPlayerDeath(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        if (player.health - e.finalDamage > 0) return

        val score = OmikujiModule.get(player)
        val th = if (score === OmikujiScore.TOKUDAIKICHI) 5 else if (score === OmikujiScore.DAIKICHI) 1 else 0

        if (Math.random() * 100 >= th) return
        val inventory = player.inventory
        val heldItem = inventory.itemInMainHand
        inventory.remove(heldItem)
        inventory.setItemInMainHand(ItemStack(Material.TOTEM_OF_UNDYING))
        object : BukkitRunnable() {
            override fun run() {
                inventory.setItemInMainHand(heldItem)
            }
        }.runTaskLater(XCorePlugin.instance, 1)
    }
}