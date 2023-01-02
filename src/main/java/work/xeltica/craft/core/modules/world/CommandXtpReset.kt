package work.xeltica.craft.core.modules.world

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.commands.CommandBase

/**
 * xtpコマンドで使う保存された位置を初期化するコマンド
 * @author Xeltica
 */
class CommandXtpReset : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1 && args.size != 2) return false
        val worldName = args[0]
        if (args.size == 1) {
            WorldModule.deleteSavedLocation(worldName)
            return true
        }
        val p = Bukkit.getPlayer(args[1])
        if (p == null) {
            sender.sendMessage("§cプレイヤーが存在しません")
            return true
        }
        WorldModule.deleteSavedLocation(worldName, p)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        if (args.size == 1) {
            // toListしてる
            val worlds = XCorePlugin.instance.server.worlds.map { it.name }.toList()
            // arrayList
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], worlds, completions)
            completions.sort()
            return completions
        } else if (args.size == 2) {
            return null
        }
        return COMPLETE_LIST_EMPTY
    }
}