package work.xeltica.craft.core.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.HintStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.models.Hint.HintType
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * ヒントアプリを開くコマンド
 * @author Xeltica
 */
class CommandHint : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null
        val store = HintStore.getInstance()
        val hints = Stream.of(*Hint.values())
        if (subCommand != null) {
            // 指定されたIDのヒントの詳細をプレイヤーに表示
            val optionalHint = hints.filter { h: Hint -> subCommand.equals(h.name, ignoreCase = true) }
                .findFirst()
            if (optionalHint.isEmpty) {
                player.sendMessage("ヒントが存在しません。")
                return true
            }
            val hint = optionalHint.get()
            var content = hint.description
            if (hint.power > 0) {
                content += """

§a§l報酬: §r§d${hint.power} エビパワー"""
                if (store.hasAchieved(player, hint)) {
                    content += """
                        
                        §6§o✧達成済み✧
                        """.trimIndent()
                }
            }
            Gui.getInstance().openDialog(
                player,
                "§l" + hint.getName() + "§r",
                content
            ) { player.performCommand("hint") }
        } else {
            // ヒント一覧をプレイヤーに表示
            val items = hints.map { h: Hint ->
                val isAchieved = store.hasAchieved(player, h)
                val isQuest = h.power > 0
                val name = h.getName() + if (isQuest) " (" + h.power + "EP)" else ""
                val icon = getIcon(h, isAchieved)
                val onClick = Consumer { m: MenuItem? -> player.performCommand("hint " + h.name) }
                MenuItem(name, onClick, icon, null, 1, isAchieved)
            }.toList()
            Gui.getInstance().openMenu(player, "ヒント", items)
        }
        return true
    }

    private fun getIcon(h: Hint, isAchieved: Boolean): Material {
        return if (h.power == 0) Material.NETHER_STAR else when (h.type) {
            HintType.NORMAL -> if (isAchieved) Material.GOLD_BLOCK else Material.GOLD_NUGGET
            HintType.CHALLENGE -> if (isAchieved) Material.DIAMOND_BLOCK else Material.DIAMOND
            else -> Material.STONE_BUTTON
        }
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return Stream.of(*Hint.values()).map { obj: Hint -> obj.toString() }
            .toList()
    }
}