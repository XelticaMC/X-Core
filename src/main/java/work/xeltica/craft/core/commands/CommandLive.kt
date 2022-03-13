package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.COMPLETE_LIST_ONOFF
import work.xeltica.craft.core.stores.PlayerStore
import work.xeltica.craft.core.gui.Gui

/**
 * 配信モードを切り替えるコマンド
 * @author Xeltica
 */
class CommandLive : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args[0] != "on" && args[0] != "off") return false
        val isLiveMode = args[0] == "on"
        val store = PlayerStore.getInstance()
        if (store.isLiveMode(player) == isLiveMode) {
            return Gui.getInstance().error(player, "既に" + (if (isLiveMode) "オン" else "オフ") + "です")
        }
        store.setLiveMode(player, isLiveMode)
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return if (args.size != 1) COMPLETE_LIST_EMPTY else COMPLETE_LIST_ONOFF
    }
}