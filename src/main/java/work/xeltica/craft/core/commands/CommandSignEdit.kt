package work.xeltica.craft.core.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import java.util.Arrays
import java.util.LinkedList

/**
 * 看板を編集するコマンド
 * @author Xeltica
 */
class CommandSignEdit : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        val block = player.getTargetBlock(null, 5)
        if (!Tag.SIGNS.isTagged(block.type)) {
            player.sendMessage(ChatColor.RED.toString() + "変更する対象の看板を見てください")
            return true
        }
        val state = block.state as Sign
        try {
            val index = args[0].toInt()
            if (index < 0 || index > 3) {
                player.sendMessage(ChatColor.RED.toString() + "行番号には0,1,2,3を指定してください")
                return true
            }
            val l = LinkedList(Arrays.asList(*args))
            l.removeAt(0)
            val line = java.lang.String.join(" ", l)
            state.line(index, Component.text(line))
            // Spigot イベントを発行し、他のプラグインにキャンセルされたらやめる
            val e = SignChangeEvent(block, player, state.lines())
            Bukkit.getPluginManager().callEvent(e)
            if (!e.isCancelled) {
                state.update()
                player.sendMessage("看板の" + index + "行目を「" + line + "」と書き換えました")
            } else {
                player.sendMessage("何らかの理由でこの看板は編集できません")
            }
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        val errorNonSign = listOf("変更する対象の看板を見てください")
        val errorOutOfBounds = listOf("第一引数は0,1,2,3のいずれかにしてください")
        if (args.size < 2) return COMMANDS
        return if (commandSender is Player) {
            val block: Block = commandSender.getTargetBlock(null, 5)
            if (!Tag.SIGNS.isTagged(block.type)) return errorNonSign
            if (!listOf("0", "1", "2", "3").contains(args[0])) return errorOutOfBounds
            val n = args[0].toInt()
            val state = block.state as Sign
            listOf(PlainTextComponentSerializer.plainText().serialize(state.line(n)))
        } else {
            COMPLETE_LIST_EMPTY
        }
    }

    companion object {
        private val COMMANDS = listOf("0", "1", "2", "3")
    }
}