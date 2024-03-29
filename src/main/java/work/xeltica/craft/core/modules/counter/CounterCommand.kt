package work.xeltica.craft.core.modules.counter

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.modules.ranking.Ranking
import work.xeltica.craft.core.modules.ranking.RankingModule
import java.io.IOException

class CounterCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        val subCommand = args[0].lowercase()
        val record = PlayerStore.open(player)
        val ui = getInstance()

        try {
            when (subCommand) {
                "register" -> {
                    if (args.size < 2) return ui.error(player, "/counter register <name> [\"daily\"]")
                    val name = args[1]
                    val isDaily = args.size == 3 && "daily".equals(args[2], ignoreCase = true)
                    record[CounterModule.PS_KEY_MODE] = true
                    record[CounterModule.PS_KEY_NAME] = name
                    record[CounterModule.PS_KEY_IS_DAILY] = isDaily
                    player.sendMessage("カウンター登録モードは有効。カウンターの始点にする感圧板をクリックかタップしてください。")
                    player.sendMessage("キャンセルする際には、 /counter cancel を実行します。")
                }

                "cancel" -> {
                    record.delete(CounterModule.PS_KEY_MODE)
                    record.delete(CounterModule.PS_KEY_NAME)
                    record.delete(CounterModule.PS_KEY_IS_DAILY)
                    record.delete(CounterModule.PS_KEY_LOCATION)
                    player.sendMessage("カウンター登録モードを無効化し、キャンセルしました。")
                }

                "unregister" -> {
                    if (args.size != 2) return ui.error(player, "/counter unregister <name>")
                    val name = args[1]
                    val data = CounterModule[name] ?: return ui.error(player, name + "という名前のカウンターは存在しません。")
                    CounterModule.remove(data)
                    player.sendMessage(name + "を登録解除しました。")
                }

                "bind" -> {
                    if (args.size != 4) return ui.error(
                        player,
                        "/counter bind <name> <all/java/bedrock/uwp/phone> <rankingName>"
                    )
                    val name = args[1].lowercase()
                    val playerType = args[2].lowercase()
                    val rankingName = args[3]
                    val data = CounterModule[name] ?: return ui.error(player, name + "という名前のカウンターは存在しません。")
                    when (playerType) {
                        "all" -> {
                            data.javaRankingId = rankingName
                            data.bedrockRankingId = rankingName
                        }

                        "java" -> data.javaRankingId = rankingName
                        "bedrock" -> data.bedrockRankingId = rankingName
                        "uwp" -> data.uwpRankingId = rankingName
                        "phone" -> data.phoneRankingId = rankingName
                    }
                    CounterModule.update(data)
                    player.sendMessage("カウンターをランキングに紐付けました。")
                }

                "unbind" -> {
                    if (args.size != 3) return ui.error(player, "/counter unbind <name> <all/java/bedrock/uwp/phone>")
                    val name = args[1].lowercase()
                    val playerType = args[2].lowercase()
                    val data = CounterModule[name] ?: return ui.error(player, name + "という名前のカウンターは存在しません。")
                    when (playerType) {
                        "all" -> {
                            data.javaRankingId = null
                            data.bedrockRankingId = null
                        }

                        "java" -> data.javaRankingId = null
                        "bedrock" -> data.bedrockRankingId = null
                        "uwp" -> data.uwpRankingId = null
                        "phone" -> data.phoneRankingId = null
                    }
                    CounterModule.update(data)
                    player.sendMessage("カウンターをランキングから解除ました。")
                }

                "info" -> {
                    if (args.size != 2) return ui.error(player, "/counter info <name>")
                    val name = args[1]
                    val data = CounterModule[name] ?: return true
                    player.sendMessage("名前: $name")
                    player.sendMessage("始点: " + data.location1.toString())
                    player.sendMessage("終点: " + data.location2.toString())
                    player.sendMessage("1日1回かどうか: " + data.isDaily)
                    player.sendMessage("紐付いたランキングID: ")
                    player.sendMessage(" Java: " + data.javaRankingId)
                    player.sendMessage(" Bedrock: " + data.bedrockRankingId)
                    player.sendMessage(" UWP: " + data.uwpRankingId)
                    player.sendMessage(" Phone: " + data.phoneRankingId)
                }

                "resetdaily" -> {
                    if (args.size != 2) {
                        CounterModule.resetAllPlayersPlayedLog()
                        player.sendMessage("全プレイヤーのプレイ済み履歴を削除しました。")
                    } else {
                        val name = args[1]
                        val p = Bukkit.getPlayerUniqueId(name)
                        if (p == null) {
                            ui.error(player, "サーバーがUUIDを取得できなかったため、プレイ済み履歴を削除できませんでした。")
                            return true
                        }
                        PlayerStore.open(p).delete(CounterModule.PS_KEY_COUNT)
                        player.sendMessage("そのプレイヤーのプレイ済み履歴を削除しました。")
                    }
                }

                "list" -> {
                    val list = CounterModule.getCounters()
                    if (list.isEmpty()) {
                        ui.error(player, "カウンターはまだ作成されていません。")
                    } else {
                        player.sendMessage("合計: " + list.size)
                        list.forEach {
                            player.sendMessage("* ${it.name}")
                        }
                    }
                }

                "setisdaily" -> {
                    return ui.error(player, "未実装")
                }

                else -> {
                    return false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ui.error(player, "${ChatColor.RED}IO エラーが発生したために処理を続行できませんでした。")
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.isEmpty()) return COMPLETE_LIST_EMPTY
        val subcommand = args[0].lowercase()
        if (args.size == 1) {
            val commands = listOf("register", "unregister", "cancel", "bind", "unbind", "info", "list", "resetdaily")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(subcommand, commands, completions)
            completions.sort()
            return completions
        } else if (args.size == 2) {
            when (subcommand) {
                "unregister", "info", "bind", "unbind" -> {
                    return CounterModule.getCounters().stream().map(CounterData::name).toList()
                }
            }
        } else if (args.size == 3) {
            when (subcommand) {
                "bind", "unbind" -> {
                    val playerType = args[2].lowercase()
                    val types = listOf("all", "java", "bedrock", "uwp", "phone")
                    val completions = ArrayList<String>()
                    StringUtil.copyPartialMatches(playerType, types, completions)
                    completions.sort()
                    return completions
                }
            }
        } else if (args.size == 4) {
            if ("bind" == subcommand) {
                val rankings = RankingModule.getAll().stream().map { obj: Ranking -> obj.name }.toList()
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(args[3], rankings, completions)
                completions.sort()
                return completions
            }
        }
        return COMPLETE_LIST_EMPTY
    }
}