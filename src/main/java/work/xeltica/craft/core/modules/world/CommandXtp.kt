package work.xeltica.craft.core.modules.world

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.commands.CommandBase
import work.xeltica.craft.core.modules.world.WorldModule.teleportToSavedLocation

/**
 * 指定したワールドの最後にいた場所に転送するコマンド
 * @author Lutica
 */
class CommandXtp : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1 && args.size != 2) return false
        if (args.size == 2 && !sender.hasPermission("otanoshimi.command.xtp.other")) {
            sender.sendMessage("§c権限がありません。")
            return true
        }
        if (args.size == 1 && sender !is Player) {
            sender.sendMessage("プレイヤーが実行してください。")
            return true
        }
        val worldName = args[0]
        val p = if (args.size == 2) Bukkit.getPlayer(args[1]) else sender as Player
        if (p == null) {
            sender.sendMessage("§cプレイヤーが存在しません")
            return true
        }
        teleportToSavedLocation(p, worldName)
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1) {
            val worlds = instance.server.worlds.map { it.name }.toList()
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