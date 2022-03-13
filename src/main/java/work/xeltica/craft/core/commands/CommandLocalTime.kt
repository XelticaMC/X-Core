package work.xeltica.craft.core.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import java.lang.NumberFormatException
import java.util.*

/**
 * 現在いるワールドのみの時間を操作するコマンド
 * @author Xeltica
 */
class CommandLocalTime : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) return false
        val world = player.world
        when (args[0].lowercase(Locale.getDefault())) {
            "set" -> {
                if (args.size != 2) return false
                val timeString = args[1]
                try {
                    val time = toTime(timeString)
                    world.time = time.toLong()
                    player.sendMessage(ChatColor.RED.toString() + "時刻を " + time + "に設定しました")
                } catch (e: NumberFormatException) {
                    player.sendMessage(ChatColor.RED.toString() + "時間指定が異常です")
                }
            }
            "add" -> {
                if (args.size != 2) return false
                val timeString = args[1]
                try {
                    val time = toTime(timeString)
                    world.time = world.time + time
                    player.sendMessage(ChatColor.RED.toString() + "時刻を " + world.time + "に設定しました")
                } catch (e: NumberFormatException) {
                    player.sendMessage(ChatColor.RED.toString() + "時間指定が異常です")
                }
            }
            "query" -> {
                if (args.size != 1) return false
                player.sendMessage(world.time.toString())
            }
            else -> {
                return false
            }
        }
        return true
    }

    /**
     * 対応する文字列から時間の数値に変換する関数
     * @param timeString builtinTimeMapにある対応する文字列
     * @return 時間の数値
     */
    private fun toTime(timeString: String): Int {
        return BUILTIN_TIME_MAP[timeString] ?: timeString.toInt()
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            val commands = listOf("set", "add", "query")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], commands, completions)
            completions.sort()
            return completions
        } else if (args.size == 2) {
            if (args[1] == "set") {
                val times = listOf("day", "night", "noon", "midnight", "sunrise", "sunset")
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[1], times, completions)
                completions.sort()
                return completions
            }
        }
        return COMPLETE_LIST_EMPTY
    }
}

val BUILTIN_TIME_MAP = mapOf(
    "day" to 1000,
    "night" to 13000,
    "noon" to 6000,
    "midnight" to 18000,
    "sunrise" to 23000,
    "sunset" to 12000,
)