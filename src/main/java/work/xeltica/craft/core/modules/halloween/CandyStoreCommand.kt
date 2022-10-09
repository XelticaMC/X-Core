package work.xeltica.craft.core.modules.halloween

import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

class CandyStoreCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        return false
    }
}