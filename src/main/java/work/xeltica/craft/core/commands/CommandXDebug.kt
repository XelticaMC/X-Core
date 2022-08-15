package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.utils.EventUtility
import java.time.LocalDate

class CommandXDebug : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val startEventDay = LocalDate.of(2022, 8, 15)
        val endEventDay = LocalDate.of(2022, 9, 1)
        sender.sendMessage("now ${LocalDate.now()}")
        sender.sendMessage("startEventDay ${startEventDay}")
        sender.sendMessage("endEventDay ${endEventDay}")
        sender.sendMessage(if (EventUtility.isEventNow()) "現在イベント中" else "イベント中でない")
        return true
    }
}