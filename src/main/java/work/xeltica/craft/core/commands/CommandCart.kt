package work.xeltica.craft.core.commands

import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.data.Rail
import org.bukkit.block.data.type.RedstoneRail
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
 * トロッコを出現させるコマンド
 * @author Xeltica
 */
class CommandCart : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (!canSummonVehicles(player.world)) {
            return getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        val lookBlock = player.getTargetBlock(5) ?: return true
        if (lookBlock.type.data == Rail::class.java || lookBlock.type.data == RedstoneRail::class.java) {
            val spawnLoc = lookBlock.location.add(0.0, 0.5, 0.0)
            spawnLoc.world.spawnEntity(spawnLoc, EntityType.MINECART, CreatureSpawnEvent.SpawnReason.CUSTOM)
            player.sendMessage("トロッコを召喚した。")
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
            achieve(player, Hint.MINECART)
        } else {
            return getInstance().error(player, "ここには召喚できない")
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_EMPTY
    }
}