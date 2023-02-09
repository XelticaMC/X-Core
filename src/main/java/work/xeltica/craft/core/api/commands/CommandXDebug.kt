package work.xeltica.craft.core.api.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * X-Core デバッグコマンドです。
 */
class CommandXDebug : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return true
    }
}