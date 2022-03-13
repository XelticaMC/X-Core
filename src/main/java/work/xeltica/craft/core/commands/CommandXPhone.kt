package work.xeltica.craft.core.commands

import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.stores.ItemStore
import work.xeltica.craft.core.stores.HintStore
import java.util.*

/**
 * X Phoneを受け取るコマンド
 * @author Xeltica
 */
class CommandXPhone : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val item = ItemStore.getInstance().getItem("xphone")
        if (item != null) {
            player.inventory.addItem(item)
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            Objects.requireNonNull(item.itemMeta.displayName())
                ?.let { player.sendMessage(it.append(Component.text("を付与しました"))) }
            HintStore.getInstance().achieve(player, Hint.TWIN_XPHONE)
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_EMPTY
    }
}