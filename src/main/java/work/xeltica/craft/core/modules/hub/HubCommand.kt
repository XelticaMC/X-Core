package work.xeltica.craft.core.modules.hub

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

/**
 * ロビーへ移動するコマンド
 * @author Lutica
 */
class HubCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        HubModule.teleport(player, HubType.Main)
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return COMPLETE_LIST_EMPTY
    }
}