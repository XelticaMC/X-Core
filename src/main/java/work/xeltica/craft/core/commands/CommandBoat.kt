package work.xeltica.craft.core.commands

import com.destroystokyo.paper.block.TargetBlockInfo
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.BlockFace
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule.achieve
import work.xeltica.craft.core.modules.world.WorldModule.canSummonVehicles

/**
 * ボートを出現させるコマンド
 * @author Xeltica
 */
class CommandBoat : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (!canSummonVehicles(player.world)) {
            return getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        var spawnLoc = player.location
        val lookBlock = player.getTargetBlock(5, TargetBlockInfo.FluidMode.ALWAYS)
        if (lookBlock != null && lookBlock.type != Material.AIR) {
            val lookFace = player.getTargetBlockFace(5, TargetBlockInfo.FluidMode.ALWAYS)
            spawnLoc = lookBlock.location
            if (lookFace != null) {
                spawnLoc.add(lookFace.direction)
                spawnLoc.add(0.5, 0.0, 0.5)
            }
        }
        if (collidesAt(spawnLoc.clone())) {
            var flag = false
            for (offset in offsets) {
                if (!collidesAt(spawnLoc.clone().add(offset!!))) {
                    spawnLoc = spawnLoc.add(offset)
                    flag = true
                    break
                }
            }
            if (!flag) return getInstance().error(player, "狭すぎて置けない…")
        }
        val boat = spawnLoc.world.spawnEntity(spawnLoc, EntityType.BOAT, CreatureSpawnEvent.SpawnReason.CUSTOM)
        boat.setRotation(player.location.yaw, boat.location.pitch)
        player.sendMessage("ボートを足元に召喚した。")
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
        achieve(player, Hint.BOAT)
        return true
    }

    private fun collidesAt(loc: Location): Boolean {
        if (loc.block.isSolid) return true
        for (vec in collisionArea) {
            if (loc.clone().add(vec).block.isSolid) return true
        }
        return false
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return COMPLETE_LIST_EMPTY
    }

    companion object {
        private val collisionArea = java.util.List.of(
            BlockFace.EAST.direction.multiply(0.7),
            BlockFace.WEST.direction.multiply(0.7),
            BlockFace.SOUTH.direction.multiply(0.7),
            BlockFace.NORTH.direction.multiply(0.7)
        )
        private val offsets = java.util.List.of(
            BlockFace.NORTH.direction.multiply(0.35),
            BlockFace.NORTH_EAST.direction.multiply(0.35),
            BlockFace.EAST.direction.multiply(0.35),
            BlockFace.SOUTH_EAST.direction.multiply(0.35),
            BlockFace.SOUTH.direction.multiply(0.35),
            BlockFace.SOUTH_WEST.direction.multiply(0.35),
            BlockFace.WEST.direction.multiply(0.35),
            BlockFace.NORTH_WEST.direction.multiply(0.35)
        )
    }
}