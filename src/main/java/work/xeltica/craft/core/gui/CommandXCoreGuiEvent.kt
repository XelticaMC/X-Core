package work.xeltica.craft.core.gui

import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance

/**
 * Java版で看板を使用したダイアログのボタンを
 * クリックしたときに内部的に送られるコマンド
 * プレイヤーが使用することは想定していない
 * @author Xeltica
 */
class CommandXCoreGuiEvent : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) return false
        getInstance().handleCommand(args[0])
        return true
    }
}