package work.xeltica.craft.core.modules.item

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

class GiveCustomItemCommand: CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) return true
        val p = Bukkit.getPlayer(args[0])
        if (p == null) {
            player.sendMessage(Component.text(ChatColor.RED.toString() + "そのようなプレイヤーはいません"))
            return true
        }
        try {
            val typeString = args[1]
            val item = ItemModule.getItem(typeString.lowercase())
            p.inventory.addItem(item)
            p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            p.sendMessage(item.displayName().append(Component.text("を付与しました")))

        } catch (e: IllegalArgumentException) {
            player.sendMessage("引数がおかしい")
            return true
        }
        return true
    }
}