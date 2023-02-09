package work.xeltica.craft.core.modules.ranking

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandBase
import java.io.IOException

class RankingCommand : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        val subCommand = args[0].lowercase()
        val api = RankingModule
        val id = if (sender is Player) sender.uniqueId.toString() else null

        try {
            when (subCommand) {
                "create" -> {
                    if (args.size < 3) {
                        sender.sendMessage("/ranking create <name> <displayName> [\"playermode\"]")
                        return true
                    }
                    val name = args[1].trim()
                    val displayName = args[2].trim()
                    val isPlayerMode = (args.size >= 4 && args[3].lowercase() == "playermode")

                    if (api.has(name)) {
                        sender.sendMessage("既に存在します。")
                        return true
                    }
                    api.create(name, displayName, isPlayerMode)
                    sender.sendMessage("ランキング " + name + "を" + (if (isPlayerMode) "プレイヤーモードで" else "") + "作成しました。")
                }

                "delete" -> {
                    if (args.size != 2) {
                        sender.sendMessage("/ranking delete <name>")
                        return true
                    }
                    val name = args[1].trim { it <= ' ' }

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。")
                        return true
                    }
                    sender.sendMessage(if (api.delete(name)) "削除に成功しました。" else "削除に失敗しました。")
                }

                "query" -> {
                    if (args.size != 2) {
                        sender.sendMessage("/ranking query <name>")
                        return true
                    }
                    val name = args[1].trim { it <= ' ' }

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。")
                        return true
                    }
                    val ranking = api[name]?.queryRanking() ?: return true
                    for (i in ranking.indices) {
                        val (id1, score) = ranking[i]
                        sender.sendMessage("${ChatColor.GOLD}%d位:${ChatColor.GREEN}%s ${ChatColor.AQUA}%s".format(i + 1, id1, score))
                    }
                }

                "list" -> {
                    val list = api.getAll()
                    if (list.isEmpty()) {
                        sender.sendMessage("一つもありません。")
                        return true
                    }
                    list.map { "%s ${ChatColor.GRAY}(%s)".format(it.name, it.getDisplayName()) }
                        .forEach(sender::sendMessage)
                }

                "set" -> {
                    if (args.size != 3) {
                        sender.sendMessage("/ranking set <rankingName> <value>")
                        return true
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。")
                        return true
                    }
                    val name = args[1].trim { it <= ' ' }
                    try {
                        val value = args[2].toInt()
                        if (!api.has(name)) {
                            sender.sendMessage("ランキングが存在しません。")
                            return true
                        }
                        val ranking = api[name]!!
                        ranking.add(id, value)
                        sender.sendMessage("ランキングにレコードを追加しました。")
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("値には数値のみ認められます。")
                    }
                }

                "unset" -> {
                    if (args.size != 2) {
                        sender.sendMessage("/ranking unset <rankingName>")
                        return true
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。")
                        return true
                    }
                    val name = args[1].trim { it <= ' ' }
                    try {
                        val ranking = api[name]
                        ranking!!.remove(id)
                        sender.sendMessage("ランキングからレコードを削除しました。")
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("値には数値のみ認められます。")
                    }
                }

                "mode" -> {
                    if (args.size != 3) {
                        sender.sendMessage("/ranking mode <rankingName> <normal/time/point>")
                        return true
                    }
                    val name = args[1].trim { it <= ' ' }

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。")
                        return true
                    }
                    val mode = args[2].trim { it <= ' ' }.lowercase()
                    if (!listOf("normal", "time", "point").contains(mode)) {
                        sender.sendMessage("モードには次のものを指定できます。")
                        sender.sendMessage("normal: 値を単位の無い普通の数値とみなす")
                        sender.sendMessage("  time: 値をミリ秒時間とみなす。カウンターと併用する場合はこちら")
                        sender.sendMessage(" point: 値を点数とみなす。")
                        return true
                    }
                    api[name]!!.setMode(mode)
                    sender.sendMessage("モードを設定しました。")
                }

                "hologram" -> {
                    if (args.size < 3) {
                        sender.sendMessage("/ranking hologram <name> <sub-commands>")
                        return true
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。")
                        return true
                    }
                    val player = sender as Player
                    val name = args[1]

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。")
                        return true
                    }
                    val data = api[name] ?: return true
                    val hSubCommand = args[2].lowercase()

                    when (hSubCommand) {
                        "spawn" -> data.setHologram(player.location.add(0.0, 3.0, 0.0), data.getHologramHidden())
                        "despawn" -> data.setHologram(null, data.getHologramHidden())
                        "obfuscate" -> data.setHologram(data.getHologramLocation(), true)
                        "deobfuscate" -> data.setHologram(data.getHologramLocation(), false)
                    }
                }

                else -> {
                    return false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            sender.sendMessage("IOエラーにより失敗しました。")
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        when (args.size) {
            1 -> {
                val commands = listOf("create", "delete", "query", "list", "set", "unset", "hologram", "mode")
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[0], commands, completions)
                completions.sort()
                return completions
            }

            2 -> {
                if (listOf("delete", "query", "set", "unset", "hologram", "mode").contains(args[0])) return COMPLETE_LIST_EMPTY
                val rankings = RankingModule.getAll().map(Ranking::name).toList()
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[1], rankings, completions)
                completions.sort()
                return rankings
            }

            3 -> {
                when (args[0]) {
                    "mode" -> {
                        val modes = listOf("normal", "time", "point")
                        val completions = ArrayList<String>()
                        StringUtil.copyPartialMatches(args[2], modes, completions)
                        completions.sort()
                        return completions
                    }

                    "hologram" -> {
                        val subCommands = listOf("spawn", "despawn", "obfuscate", "deobfuscate")
                        val completions = ArrayList<String>()
                        StringUtil.copyPartialMatches(args[2], subCommands, completions)
                        completions.sort()
                        return completions
                    }
                }
            }
        }
        return COMPLETE_LIST_EMPTY
    }
}