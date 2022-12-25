package work.xeltica.craft.core.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.commands.CommandBase
import work.xeltica.craft.core.modules.world.WorldModule
import java.util.*

/**
 * xtpコマンドで使う保存された位置を初期化するコマンド
 * @author Xeltica
 */
class CommandXtpReset : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val worldModule = WorldModule
        if (args.size != 1 && args.size != 2) return false
        val worldName = args[0]
        if (args.size == 1) {
            worldModule.deleteSavedLocation(worldName)
        }
        val p = if (args.size == 2) Bukkit.getPlayer(args[1]) else null
        if (p == null) {
            sender.sendMessage("§cプレイヤーが存在しません")
            return true
        }
        worldModule.deleteSavedLocation(worldName, p)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
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