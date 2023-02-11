package work.xeltica.craft.core.modules.xphone

import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.modules.item.ItemModule.getItem

/**
 * X Phoneを受け取るコマンド
 * @author Lutica
 */
class XphoneCommand : CommandPlayerOnlyBase() {
    private val subCommands = listOf("get")
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty() && args[0] == "get") {
            // getが指定されたら、アイテムを受け取る
            val item = getItem(XphoneModule.ITEM_NAME_XPHONE)
            player.inventory.addItem(item)
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            val displayName = item.itemMeta.displayName() ?: item.displayName()
            player.sendMessage(displayName.append(Component.text("を付与しました")))
        } else {
            XphoneModule.openSpringBoard(player)
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return if (args.size > 1) COMPLETE_LIST_EMPTY else subCommands
    }
}