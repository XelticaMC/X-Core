package work.xeltica.craft.core.modules.livemode

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance

/**
 * 配信モードを切り替えるコマンド
 * @author Lutica
 */
class CommandLive : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in COMPLETE_LIST_ONOFF) return false
        val isLiveMode = args[0] == "on"
        if (LiveModeModule.isLiveMode(player) == isLiveMode) {
            return getInstance().error(player, "既に" + (if (isLiveMode) "オン" else "オフ") + "です")
        }
        LiveModeModule.setLiveMode(player, isLiveMode)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        return if (args.size != 1) COMPLETE_LIST_EMPTY else COMPLETE_LIST_ONOFF
    }
}