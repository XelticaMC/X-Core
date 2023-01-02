package work.xeltica.craft.core.modules.eventFirework

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandBase

class FireworkCommand : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args[0]) {
            "run" -> {
                if (args.size != 2) return false
                val script = EventFireworkModule.scripts[args[1]]
                if (script == null) {
                    sender.sendMessage("${ChatColor.RED}No such script named '${args[0]}'.")
                    return true
                }
                EventFireworkModule.runScript(script, sender)
            }

            "center" -> {
                if (sender !is Player) {
                    sender.sendMessage("プレイヤーが実行しなさい")
                    return true
                }
                EventFireworkModule.setCenterLocation(sender.location)
            }

            else -> return false
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.isEmpty()) return COMPLETE_LIST_EMPTY
        if (args.size == 1) {
            val commands = listOf("run", "center")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], commands, completions)
            completions.sort()
            return completions
        } else if (args.size == 2) {
            if (args[0] != "run") return COMPLETE_LIST_EMPTY
            val completions = ArrayList<String>()
            val names = EventFireworkModule.scripts.keys
            StringUtil.copyPartialMatches(args[1], names, completions)
            completions.sort()
            return completions
        }
        return COMPLETE_LIST_EMPTY
    }
}