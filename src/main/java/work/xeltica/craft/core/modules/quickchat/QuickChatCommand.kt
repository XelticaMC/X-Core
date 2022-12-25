package work.xeltica.craft.core.modules.quickchat

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandBase
import java.util.*

class QuickChatCommand: CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0].lowercase(Locale.getDefault())) {
            "register" -> {
                if (args.size < 3) return false
                if (QuickChatModule.register(args[1], args[2])) {
                    sender.sendMessage(args[1] + "に" + args[2] + "を登録しました")
                } else {
                    sender.sendMessage("既に" + args[1] + "は登録されています")
                }
            }
            "unregister" -> {
                if (args.size < 2) return false
                if (QuickChatModule.unregister(args[1])) {
                    sender.sendMessage(args[1] + "を削除しました")
                } else {
                    sender.sendMessage(args[1] + "は存在しません")
                }
            }
            "list" -> {
                sender.sendMessage("===== Quick Chat List =====")
                for (prefix in QuickChatModule.getAllPrefix()) {
                    sender.sendMessage(prefix + " : " + QuickChatModule.getMessage(prefix))
                }
            }
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.isEmpty()) return COMPLETE_LIST_EMPTY
        val subCmd = args[0].lowercase(Locale.getDefault())
        if (args.size == 1) {
            val commands = listOf("register", "unregister", "list")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(subCmd, commands, completions)
            completions.sort()
            return completions
        } else if (args.size == 2) {
            if (subCmd == "unregister") {
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[1], QuickChatModule.getAllPrefix(), completions)
                completions.sort()
                return completions
            }
        }
        return COMPLETE_LIST_EMPTY
    }
}