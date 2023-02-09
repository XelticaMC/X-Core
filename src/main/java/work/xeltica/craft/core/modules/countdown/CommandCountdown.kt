package work.xeltica.craft.core.modules.countdown

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import java.util.LinkedList

/**
 * カウントダウンコマンド
 * @author Lutica
 */
class CommandCountdown : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        try {
            val count = args[0].toInt()
            if (count > 60) {
                player.sendMessage("60秒を超えるカウントダウンを作成することはできません。")
                return true
            }
            if (count < 1) {
                player.sendMessage("1秒未満のカウントダウンを作成することはできません。")
                return true
            }
            val members = HashSet<Player>()
            members.add(player)
            if (args.size >= 2) {
                for (name in args.copyOfRange(1, args.size - 1)) {
                    val p = Bukkit.getPlayer(name)
                    if (p == null) {
                        player.sendMessage("プレイヤー $name が見つかりません。")
                        return true
                    }
                    members.add(p)
                }
            }
            CountdownModule.showCountdown(count, members, player)
            return true
        } catch (e: NumberFormatException) {
            player.sendMessage("${ChatColor.RED}第一引数には数値を指定してください")
            return true
        }
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.size <= 1) return COMPLETE_LIST_EMPTY
        val playerNames = LinkedList(Bukkit.getOnlinePlayers().map { it.name }.toList())
        val completions = ArrayList<String>()
        playerNames.removeAll(args.copyOfRange(1, args.size - 1).toSet())
        StringUtil.copyPartialMatches(args[args.size - 1], playerNames, completions)
        completions.sort()
        return completions
    }
}