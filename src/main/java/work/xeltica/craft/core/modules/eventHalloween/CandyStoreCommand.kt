package work.xeltica.craft.core.modules.eventHalloween

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

class CandyStoreCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null

        // サブコマンドがなければお店UIを開く
        if (subCommand == null || !player.hasPermission("otanoshimi.command.candystore." + subCommand.lowercase())) {
            EventHalloweenModule.openCandyStore(player)
            return true
        }

        when (subCommand.lowercase()) {
            "add" -> {
                if (args.size != 2) {
                    player.sendMessage("/candystore add <cost>")
                    return true
                }
                val cost = args[1].toInt()
                val handheld = player.inventory.itemInMainHand
                if (handheld.type == Material.AIR) {
                    player.sendMessage("アイテムを手に持っていないため追加できません。")
                    return true
                }
                EventHalloweenModule.addItem(CandyStoreItem(handheld, cost))
                player.sendMessage("追加しました。")
            }

            "delete" -> EventHalloweenModule.openCandyStoreUI(player, "削除するアイテムを選んでください") {
                if (it is CandyStoreItem) {
                    EventHalloweenModule.deleteItem(it)
                }
                player.sendMessage("削除しました。")
            }

            "getcandy" -> {
                val amount = args[1].toIntOrNull() ?: return false
                val candy = EventHalloweenModule.generateCandy(amount)
                player.inventory.addItem(candy)
                player.sendMessage("${amount}アメを渡しました。")
                player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
            }

            "event" -> {
                when (args[1].lowercase()) {
                    "on" -> EventHalloweenModule.isEventMode = true
                    "off" -> EventHalloweenModule.isEventMode = false
                    else -> return false
                }
            }

            "spawnratiomain" -> {
                val ratio = args[1].toIntOrNull() ?: return false
                EventHalloweenModule.spawnRatioInMainWorld = ratio
            }

            "spawnratioevent" -> {
                val ratio = args[1].toIntOrNull() ?: return false
                EventHalloweenModule.spawnRatioInEventWorld = ratio
            }
        }
        return true
    }
}