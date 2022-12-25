package work.xeltica.craft.core.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import java.util.*

/**
 * 現在いるワールドのみの時間を操作するコマンド
 * @author Xeltica
 */
class CommandLocalTime : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        val world = player.world
        val subCommand = args[0].lowercase(Locale.getDefault())
        when (subCommand) {
            "set" -> {
                if (args.size != 2) return false
                val timeString = args[1]
                try {
                    val time = toTime(timeString)
                    world.time = time.toLong()
                    player.sendMessage(ChatColor.RED.toString() + "時刻を${time}に設定しました")
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
                    player.sendMessage(ChatColor.RED.toString() + "時刻を${world.time}に設定しました")
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
        return if (builtinTimeMap.containsKey(timeString)) {
            builtinTimeMap[timeString]!!
        } else timeString.toInt()
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            val commands: List<String> = mutableListOf("set", "add", "query")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], commands, completions)
            Collections.sort(completions)
            return completions
        } else if (args.size == 2) {
            if (args[1] == "set") {
                val times: List<String> = mutableListOf("day", "night", "noon", "midnight", "sunrise", "sunset")
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[1], times, completions)
                Collections.sort(completions)
                return completions
            }
        }
        return COMPLETE_LIST_EMPTY
    }

    /**
     * 組込み名前付き時間を追加
     * 統合版のほうが充実しているので統合版から拝借してます
     */
    init {
        builtinTimeMap["day"] = 1000
        builtinTimeMap["night"] = 13000
        builtinTimeMap["noon"] = 6000
        builtinTimeMap["midnight"] = 18000
        builtinTimeMap["sunrise"] = 23000
        builtinTimeMap["sunset"] = 12000
    }

    companion object {
        /**
         * 時間の数値とそれに対応するmidnightなどの文字列が格納されている
         */
        private val builtinTimeMap = HashMap<String, Int>()
    }
}