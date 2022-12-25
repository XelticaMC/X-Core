package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.stores.NickNameStore
import java.util.*

/**
 * @author raink1208
 */
class CommandNickName : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        NickNameStore.getInstance().setNickNameType(player.uniqueId, args[0])
        player.sendMessage("NickNameTypeを" + args[0] + "に変更しました")
        NickNameStore.getInstance().setNickName(player)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        if (args.size != 1) return COMPLETE_LIST_EMPTY
        val completions = ArrayList<String>()
        StringUtil.copyPartialMatches(args[0], COMMANDS, completions)
        completions.sort()
        return completions
    }

    companion object {
        private val COMMANDS: List<String> = ArrayList(mutableListOf("minecraft", "discord", "discord-nick"))
    }
}