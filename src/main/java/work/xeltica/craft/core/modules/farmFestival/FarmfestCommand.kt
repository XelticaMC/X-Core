package work.xeltica.craft.core.modules.farmFestival

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.XCoreException
import work.xeltica.craft.core.api.commands.CommandBase
import java.util.ArrayList

class FarmfestCommand : CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        when (args[0]) {
            "init" -> {
                FarmFestivalModule.init()
            }
            "clearFarm" -> {
                FarmFestivalModule.clearFarm()
                sender.sendMessage("畑を初期化しました。")
            }
            "add" -> {
                for (name in args.drop(1)) {
                    val p = Bukkit.getPlayer(name)
                    if (p == null) {
                        sender.sendMessage("$name は存在しません。")
                        continue
                    }
                    try {
                        FarmFestivalModule.addPlayerToBoard(p)
                        sender.sendMessage("$name を追加しました。")
                    } catch (e: XCoreException) {
                        sender.sendMessage(ChatColor.RED.toString() + e.message)
                        if (sender is Player) {
                            sender.playSound(sender.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                        }
                    }
                }
            }
            "start" -> {
                if (FarmFestivalModule.isPlaying) {
                    sender.sendMessage("${ChatColor.RED}もう始まっている！")
                    return true
                }
                try {
                    FarmFestivalModule.start()
                    sender.sendMessage("開始しました。")
                } catch (e: XCoreException) {
                    sender.sendMessage(ChatColor.RED.toString() + e.message)
                    if (sender is Player) {
                        sender.playSound(sender.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                    }
                }
            }
            "stop" -> {
                if (!FarmFestivalModule.isPlaying) {
                    sender.sendMessage("${ChatColor.RED}もう終わっている！")
                    return true
                }
                FarmFestivalModule.stop()
                FarmFestivalModule.board.forEach {
                    sender.sendMessage("${it.key.name}: ${it.value}ポイント")
                }
            }
            else -> return false
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        if (args.isEmpty()) return COMPLETE_LIST_EMPTY
        val subCommand = args[0]
        if (args.size == 1) {
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(subCommand, commands, completions)
            completions.sort()
            return completions
        } else if (subCommand == "add") {
            return null
        }
        return COMPLETE_LIST_EMPTY
    }

    private val commands = listOf(
        "clearFarm",
        "add",
        "init",
        "start",
        "stop",
    )
}