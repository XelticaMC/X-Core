package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import work.xeltica.craft.core.api.commands.CommandBase
import work.xeltica.craft.core.modules.fireworkFestival.FireworkFestivalModule
import work.xeltica.craft.core.modules.mobball.MobBallModule
import work.xeltica.craft.core.modules.notification.NotificationModule

class CommandXReload : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        if (args[0] == "all" || args[0] == "mobball") MobBallModule.reload()
        if (args[0] == "all" || args[0] == "notification") NotificationModule.reload()
        if (args[0] == "all" || args[0] == "firework") FireworkFestivalModule.reload()
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): MutableList<String> {
        return if (args.size == 1) mutableListOf("all", "mobball", "notification", "firework") else mutableListOf()
    }
}