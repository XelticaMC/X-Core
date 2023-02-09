package work.xeltica.craft.core.modules.hint

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.hint.Hint.HintType
import java.util.stream.Stream

/**
 * ヒントアプリを開くコマンド
 * @author Lutica
 */
class HintCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null
        val module = HintModule
        val hints = Hint.values()
        if (subCommand != null) {
            // 指定されたIDのヒントの詳細をプレイヤーに表示
            val hint = hints.firstOrNull { subCommand.equals(it.name, ignoreCase = true) }
            if (hint == null) {
                player.sendMessage("ヒントが存在しません。")
                return true
            }
            var content = hint.description
            if (hint.power > 0) {
                content += """

§a§l報酬: §r§d${hint.power} エビパワー"""
                if (module.hasAchieved(player, hint)) {
                    content += """
                        
                        §6§o✧達成済み✧
                        """.trimIndent()
                }
            }
            Gui.getInstance().openDialog(
                player,
                "§l" + hint.hintName + "§r",
                content
            ) { player.performCommand("hint") }
        } else {
            // ヒント一覧をプレイヤーに表示
            val items = hints.map { hint ->
                val isAchieved = module.hasAchieved(player, hint)
                val isQuest = hint.power > 0
                val name = hint.hintName + if (isQuest) " (" + hint.power + "EP)" else ""
                val icon = getIcon(hint, isAchieved)
                val onClick: (MenuItem) -> Unit = { player.performCommand("hint ${hint.name}") }
                MenuItem(name, onClick, icon, null, 1, isAchieved)
            }.toList()
            Gui.getInstance().openMenu(player, "ヒント", items)
        }
        return true
    }

    private fun getIcon(h: Hint, isAchieved: Boolean): Material {
        return when (h.type) {
            HintType.NORMAL -> if (isAchieved) Material.GOLD_BLOCK else Material.GOLD_NUGGET
            HintType.CHALLENGE -> if (isAchieved) Material.DIAMOND_BLOCK else Material.DIAMOND
            HintType.HELP -> Material.NETHER_STAR
        }
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return Stream.of(*Hint.values()).map { obj: Hint -> obj.toString() }
            .toList()
    }
}