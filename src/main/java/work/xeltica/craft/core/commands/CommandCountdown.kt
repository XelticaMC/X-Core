package work.xeltica.craft.core.commands

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.stores.HintStore
import java.lang.NumberFormatException
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.utils.Ticks
import java.lang.Runnable
import java.util.*
import java.util.function.Consumer

/**
 * カウントダウンコマンド
 * @author Xeltica
 */
class CommandCountdown : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        return if (args.isEmpty()) false else try {
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
                for (name in Arrays.copyOfRange(args, 1, args.size - 1)) {
                    val p = Bukkit.getPlayer(name)
                    if (p == null) {
                        player.sendMessage("プレイヤー $name が見つかりません。")
                        return true
                    }
                    members.add(p)
                }
            }
            members.forEach(Consumer { member: Player ->
                member.sendTitle(args[0], "", 0, 20, 0)
                member.playSound(member.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.6f)
            })
            countDown(count - 1, members)
            HintStore.getInstance().achieve(player, Hint.COUNTDOWN)
            true
        } catch (e: NumberFormatException) {
            player.sendMessage("§c第一引数には数値を指定してください")
            true
        }
    }

    private fun countDown(times: Int, members: Set<Player>) {
        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), Runnable {
            if (times > 0) {
                members.forEach(Consumer { member: Player ->
                    member.sendTitle(Integer.toString(times), "", 0, 20, 0)
                    member.playSound(member.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.6f)
                })
                countDown(times - 1, members)
            } else {
                members.forEach(Consumer { member: Player ->
                    member.sendTitle("GO!", "", 0, 20, 0)
                    member.playSound(member.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1.2f)
                })
            }
        }, Ticks.from(1.0).toLong())
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.size <= 1) return COMPLETE_LIST_EMPTY
        val players = XCorePlugin.getInstance().server.onlinePlayers.stream().map { obj: HumanEntity -> obj.name }
            .toList()
        val completions = ArrayList<String>()
        players.removeAll(Arrays.stream(args.copyOfRange(1, args.size - 1)).toList())
        StringUtil.copyPartialMatches(args[args.size - 1], players, completions)
        completions.sort()
        return completions
    }
}