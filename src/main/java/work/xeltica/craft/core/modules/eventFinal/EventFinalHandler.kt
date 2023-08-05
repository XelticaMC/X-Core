package work.xeltica.craft.core.modules.eventFinal

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Strider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.FloodgateHook.isFloodgatePlayer
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.counter.PlayerCounterFinish
import work.xeltica.craft.core.modules.counter.PlayerCounterStart
import work.xeltica.craft.core.modules.eventFirework.EventFireworkModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.utils.Ticks

class EventFinalHandler : Listener {
    @EventHandler
    fun onCounterStart(e: PlayerCounterStart) {
        if (!e.player.world.isEvent()) return
        if (e.counter.name != EventFinalModule.COUNTER_NAME) return
        EventFinalModule.sessions[e.player.uniqueId] = Session(Phase.GROUND_MARATHON)
        e.player.showTitle(Title.title(Component.text("${ChatColor.GREEN}多種目タイムアタック"), Component.text("誰よりも早くゴールを目指そう！")))
        val cancelItem = ItemStack(Material.BARRIER)
        cancelItem.editMeta {
            it.displayName(Component.text("リタイア"))
        }
        e.player.inventory.setItem(8, cancelItem)
        object : BukkitRunnable() {
            override fun run() {
                EventFinalModule.updatePhase(e.player, Phase.GROUND_MARATHON)
            }
        }.runTaskLater(XCorePlugin.instance, Ticks.from(3.0).toLong())
    }

    @EventHandler
    fun onCounterFinish(e: PlayerCounterFinish) {
        if (!e.player.world.isEvent()) return
        if (e.counter.name != EventFinalModule.COUNTER_NAME) return
        e.player.inventory.setItem(8, null)
        EventFinalModule.sessions.remove(e.player.uniqueId)
        EventFinalModule.finalize(e.player)
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (!e.entity.world.isEvent()) return
        val player = e.entity as? Player ?: return
        val session = EventFinalModule.sessions[player.uniqueId] ?: return

        e.damage = 0.0
        e.isCancelled = true

        when (e.cause) {
            DamageCause.LAVA -> {
                if (session.phase != Phase.BEFORE_LAVA) return
                if (player.isFloodgatePlayer()) {
                    player.sendMessage("統合版ではストライダーを操作できない不具合があるため、迂回ルートを用意しています。戻った先で、ブラックストーンおよび松明を通ってください。")
                    player.teleport(Location(player.world, 16080.0, -14.0, 823.0, 90f, 0f))
                    return
                }
                player.world.spawnEntity(player.location, EntityType.STRIDER, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    if (it !is Strider) return@spawnEntity
                    it.setSaddle(true)
                    it.addPassenger(player)
                    player.inventory.setItem(0, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1))
                    player.inventory.heldItemSlot = 0
                    player.playSound(player.location, Sound.ENTITY_STRIDER_SADDLE, SoundCategory.PLAYERS, 1f, 1f)
                    player.sendMessage("ストライダーがやってきた。このままゴールへ進もう！")
                }
                player.fireTicks = 0
                session.phase = Phase.LAVA_MAZE
            }
            DamageCause.FIRE -> {
                player.fireTicks = 0
            }
            DamageCause.FALL -> {
                if (session.phase != Phase.GROUND_MARATHON || player.location.y > 0) return
                player.playSound(player.location, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2f, 0.5f)
                EventFinalModule.updatePhase(player, Phase.BEFORE_LAVA)
            }

            else -> {  }
        }
    }

    @EventHandler
    fun onUseRetire(e: PlayerInteractEvent) {
        val player = e.player
        if (!player.world.isEvent()) return
        if (!EventFinalModule.sessions.containsKey(player.uniqueId)) return
        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return
        if (e.hand != EquipmentSlot.HAND) return
        if (e.item?.type != Material.BARRIER) return

        e.isCancelled = true

        EventFinalModule.retire(player)
    }

    @EventHandler
    fun onPlayerDismount(e: VehicleExitEvent) {
        val player = e.exited as? Player ?: return
        if (!EventFinalModule.sessions.containsKey(player.uniqueId)) return

        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerEditInventory(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (!EventFinalModule.sessions.containsKey(player.uniqueId)) return

        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        if (!EventFinalModule.sessions.containsKey(e.player.uniqueId)) return
        e.isCancelled = true
    }

    private fun World.isEvent(): Boolean {
        return this.name == "event_final"
    }
}