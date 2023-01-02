package work.xeltica.craft.core.modules.ebipowerShop

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.mobball.MobBallModule
import java.util.function.Consumer

class EpShopCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null

        if (subCommand == null || !player.hasPermission("otanoshimi.command.epshop." + subCommand.lowercase())) {
            openShop(player)
            return true
        }

        when (subCommand.lowercase()) {
            "add" -> {
                if (args.size != 2) {
                    player.sendMessage("/epshop add <cost>")
                    return true
                }
                val cost = args[1].toInt()
                val handheld = player.inventory.itemInMainHand
                if (handheld.type == Material.AIR) {
                    player.sendMessage("アイテムを手に持っていないため追加できません。")
                    return true
                }
                EbiPowerShopModule.addItem(EbiPowerItem(handheld, cost))
                player.sendMessage("追加しました")
            }

            "delete" -> {
                openShopMenu(player, "削除するアイテムを選んでください") { item ->
                    EbiPowerShopModule.deleteItem(item)
                    player.sendMessage("削除しました")
                }
            }
        }
        return true
    }

    private fun openShop(player: Player) {
        openShopMenu(player, "購入するアイテムを選んでください") { item ->

            when (EbiPowerShopModule.tryBuyItem(player, item)) {
                EbiPowerShopModule.Result.NO_ENOUGH_INVENTORY -> {
                    player.sendMessage("インベントリがいっぱいなため、購入に失敗しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }

                EbiPowerShopModule.Result.NO_ENOUGH_POWER -> {
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }

                EbiPowerShopModule.Result.SUCCESS -> {
                    player.sendMessage(Component.text("§a" + getItemName(item.item) + "§rを購入しました！"))
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
                    HintModule.achieve(player, Hint.EPSHOP)
                    if (MobBallModule.isMobBall(item.item)) {
                        HintModule.achieve(player, Hint.GET_BALL)
                    }
                }
            }
        }
    }

    private fun openShopMenu(player: Player, title: String, onChosen: Consumer<EbiPowerItem>?) {
        val ui = Gui.getInstance()
        val items = EbiPowerShopModule.shopItems.map { m ->
            val item = m.item
            val name = getItemName(item)
            val displayName = name + "x" + item.amount + "(" + m.cost + "EP)"
            MenuItem(displayName, { onChosen?.accept(m) }, item.type, null, item.amount)
        }.toList()
        ui.openMenu(player, title, items)
    }

    private fun getItemName(item: ItemStack): String? {
        val dn = item.itemMeta.displayName()
        return if (dn != null) PlainTextComponentSerializer.plainText().serialize(dn) else item.i18NDisplayName
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            val commands = listOf("add", "delete")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], commands, completions)
            completions.sort()
            return completions
        }
        return COMPLETE_LIST_EMPTY
    }
}