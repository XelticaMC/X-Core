package work.xeltica.craft.core.modules.item

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

class GiveCustomItemCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) return false
        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            player.sendMessage(Component.text(ChatColor.RED.toString() + "そのようなプレイヤーはいません"))
            return true
        }
        try {
            val item = ItemModule.getItem(args[1].lowercase())
            item.amount = if (args.size >= 3) (args[2].toIntOrNull() ?: 1) else 1

            target.run {
                inventory.addItem(item)
                playSound(location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
                sendMessage(item.displayName().append(Component.text("を付与しました")))
            }

        } catch (e: IllegalArgumentException) {
            player.sendMessage("引数がおかしい")
            return true
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1) {
            return null
        } else if (args.size == 2) {
            val itemNames = ItemModule.getCustomItemNames()
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[1], itemNames, completions)
            completions.sort()
            return completions
        }
        return COMPLETE_LIST_EMPTY
    }
}