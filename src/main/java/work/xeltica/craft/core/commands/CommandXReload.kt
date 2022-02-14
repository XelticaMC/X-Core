package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import work.xeltica.craft.core.stores.MobBallStore

class CommandXReload : CommandBase() {
    override fun execute(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) return false
        if (args[0] == "all" || args[0] == "mobball") MobBallStore.getInstance().reload()
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return if (args.isEmpty()) mutableListOf("all", "mobball") else mutableListOf();
    }
}