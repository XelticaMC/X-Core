package work.xeltica.craft.core.modules.halloween

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import java.util.*

class CandyStoreCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null

        // サブコマンドがなければお店UIを開く
        if (subCommand == null || !player.hasPermission("otanoshimi.command.candystore." + subCommand.lowercase(Locale.getDefault()))) {
            HalloweenModule.openCandyStore(player)
            return true
        }

        when (subCommand.lowercase(Locale.getDefault())) {
            "add" -> {
                if (args.size != 2) {
                    player.sendMessage("/candystore add <cost>")
                    return true
                }
                val cost = args[1].toInt()
                val handheld = player.inventory.itemInMainHand
                if (handheld == null || handheld.type == Material.AIR) {
                    player.sendMessage("アイテムを手に持っていないため追加できません。")
                    return true
                }
                HalloweenModule.addItem(CandyStoreItem(handheld, cost))
                player.sendMessage("追加しました。")
            }

            "delete" -> HalloweenModule.openCandyStoreUI(player, "削除するアイテムを選んでください") {
                HalloweenModule.deleteItem(it)
                player.sendMessage("削除しました。")
            }

            "getcandy" -> {
                val amount = args[1].toIntOrNull() ?: return false
                val candy = HalloweenModule.generateCandy(amount)
                player.inventory.addItem(candy)
                player.sendMessage("${amount}アメを渡しました。")
                player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            }
        }
        return true
    }
}