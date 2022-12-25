package work.xeltica.craft.core.modules.stamprally

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import work.xeltica.craft.core.api.commands.CommandBase

class StampCommand: CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        if (args[0] == "listDonePlayers") {
            val sb = StringBuilder("=== スタンプラリー達成者 ===\n")
            val players = StampRallyModule.getDonePlayerList()
            for (player in players) {
                sb.append(player.name)
                sb.append("\n")
            }
            sb.append("======================")
            sender.sendMessage(sb.toString())
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return mutableListOf("listDonePlayers")
    }
}