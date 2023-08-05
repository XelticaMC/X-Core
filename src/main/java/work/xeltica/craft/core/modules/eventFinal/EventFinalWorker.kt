package work.xeltica.craft.core.modules.eventFinal

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.entity.Strider
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class EventFinalWorker : BukkitRunnable() {
    override fun run() {
        EventFinalModule.sessions.keys.forEach(this::logic)
    }

    private fun logic(runnerId: UUID) {
        val session = EventFinalModule.sessions[runnerId] ?: return
        val runner = Bukkit.getPlayer(runnerId) ?: return
        // TODO: マラソンフェーズ：海抜（64m）以下になったら音楽を止める

        checkLavaPhaseGoal(runner, session)
        checkBeginSwimming(runner, session)
        checkFloorIsRail(runner, session)
        checkFloorIsIce(runner, session)
        checkFloorIsWool(runner, session)
    }

    private fun checkLavaPhaseGoal(runner: Player, session: Session) {
        if (session.phase != Phase.LAVA_MAZE) return
        val strider = runner.vehicle as? Strider ?: return
        val loc = strider.location
        if (loc.blockY == -17 && loc.blockZ >= 936) {
            strider.remove()
            runner.playSound(runner.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f)
            runner.inventory.setItem(0, null)
            runner.sendMessage("ストライダーは帰っていった…。ありがとう！")
        }
    }

    private fun checkBeginSwimming(runner: Player, session: Session) {
        if (session.phase != Phase.LAVA_MAZE && session.phase != Phase.BEFORE_LAVA) return
        if (!runner.isSwimming) return

        EventFinalModule.updatePhase(runner, Phase.SWIMMING)
    }

    private fun checkFloorIsRail(runner: Player, session: Session) {
        if (session.phase != Phase.SWIMMING) return
        if (runner.location.block.type != Material.POWERED_RAIL) return

        val world = runner.world

        world.spawnEntity(Location(world, 16065.5, 8.0, 1025.5), EntityType.MINECART, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            it.addPassenger(runner)
            val bow = ItemStack(Material.BOW)
            bow.addEnchantment(Enchantment.ARROW_INFINITE, 1)
            runner.inventory.setItem(0, bow)
            runner.inventory.setItem(1, ItemStack(Material.ARROW, 1))
        }
        EventFinalModule.updatePhase(runner, Phase.CART)
    }

    private fun checkFloorIsIce(runner: Player, session: Session) {
        if (session.phase != Phase.CART) return
        val cart = runner.vehicle as? Minecart ?: return
        val floor = runner.location.toBlockLocation().clone().add(0.0, -1.0, 0.0).block.type;
        if (floor != Material.BLUE_ICE) return

        runner.world.spawnEntity(cart.location, EntityType.BOAT, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            cart.remove()
            it.location.yaw = 270f
            it.addPassenger(runner)
            EventFinalModule.updatePhase(runner, Phase.BOAT)
            runner.inventory.setItem(0, null)
            runner.inventory.setItem(1, null)
        }
    }

    private fun checkFloorIsWool(runner: Player, session: Session) {
        if (session.phase != Phase.BOAT) return
        val boat = runner.vehicle as? Boat ?: return
        val floor = runner.location.toBlockLocation().clone().add(0.0, -1.0, 0.0).block.type;
        if (floor != Material.BLACK_WOOL && floor != Material.WHITE_WOOL) return

        boat.remove()
    }
}