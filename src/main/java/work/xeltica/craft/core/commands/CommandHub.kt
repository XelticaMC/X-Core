package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.HubStore
import work.xeltica.craft.core.models.HubType

/**
 * ロビーへ移動するコマンド
 * @author Xeltica
 */
class CommandHub : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val store = HubStore.getInstance()
        store.teleport(player, HubType.Main)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        return COMPLETE_LIST_EMPTY
    }
}