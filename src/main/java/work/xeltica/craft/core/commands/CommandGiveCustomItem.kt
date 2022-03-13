package work.xeltica.craft.core.commands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.ItemStore
import java.lang.IllegalArgumentException
import java.util.*

/**
 * カスタムアイテムをgiveするコマンド
 * @author Xeltica
 */
class CommandGiveCustomItem : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val name = if (args.isNotEmpty()) args[0] else null
        val p = if (name == null) player else Bukkit.getPlayer(name)
        val store = ItemStore.getInstance()
        if (p == null) {
            Objects.requireNonNull(player).sendMessage(ChatColor.RED.toString() + "そのようなプレイヤーはいません")
            return true
        }
        try {
            val typeString = if (args.size >= 2) args[1] else ""
            val item = store.getItem(typeString.lowercase(Locale.getDefault()))
            if (item != null) {
                p.inventory.addItem(item)
                p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
                Objects.requireNonNull(item.itemMeta.displayName())
                    ?.let { p.sendMessage(it.append(Component.text("を付与しました"))) }
            }
        } catch (e: IllegalArgumentException) {
            player.sendMessage("引数がおかしい")
            return true
        }
        return true
    }
}