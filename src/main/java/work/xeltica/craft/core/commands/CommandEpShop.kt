package work.xeltica.craft.core.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.models.EbiPowerItem
import work.xeltica.craft.core.stores.HintStore
import work.xeltica.craft.core.XCorePlugin
import java.lang.Runnable
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.Hint
import java.util.*
import java.util.function.Consumer

/**
 * エビパワーストアを開くコマンド
 * @author Xeltica
 */
class CommandEpShop : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null
        val store = EbiPowerStore.getInstance()

        // サブコマンドがなければお店UIを開く
        if (subCommand == null || !player.hasPermission("otanoshimi.command.epshop." + subCommand.lowercase(Locale.getDefault()))) {
            openShop(player)
            return true
        }
        when (subCommand.lowercase(Locale.getDefault())) {
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
                store.addItem(EbiPowerItem(handheld, cost))
                player.sendMessage("追加しました。")
            }
            "delete" -> openShopMenu(player, "削除するアイテムを選んでください") { item: EbiPowerItem? ->
                store.deleteItem(item)
                player.sendMessage("削除しました。")
            }
        }
        return true
    }

    /**
     * 購入用のUIを開きます。
     * @param player UIを開くプレイヤー
     */
    private fun openShop(player: Player) {
        openShopMenu(player, "購入するアイテムを選んでください") { item: EbiPowerItem ->
            when (EbiPowerStore.getInstance().tryBuyItem(player, item)) {
                EbiPowerStore.Result.NO_ENOUGH_INVENTORY -> {
                    player.sendMessage("インベントリがいっぱいなため、購入に失敗しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }
                EbiPowerStore.Result.NO_ENOUGH_POWER -> {
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }
                EbiPowerStore.Result.SUCCESS -> {
                    player.sendMessage(Component.text("§a" + getItemName(item.item()) + "§rを購入しました！"))
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
                    HintStore.getInstance().achieve(player, Hint.EPSHOP)
                }
                else -> {
                    player.sendMessage("謎のエラーだゾ")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }
            }
            Bukkit.getScheduler().runTask(XCorePlugin.getInstance(), Runnable { openShop(player) })
        }
    }

    /**
     * お店のメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param onChosen お店のメニューの一覧
     */
    private fun openShopMenu(player: Player, title: String, onChosen: Consumer<EbiPowerItem>?) {
        val ui = Gui.getInstance()
        val store = EbiPowerStore.getInstance()
        val items = store.shopItems
            .stream()
            .map { m: EbiPowerItem ->
                val item = m.item()
                val name = getItemName(item)
                val displayName = name + "×" + item.amount + " (" + m.cost() + "EP)"
                MenuItem(displayName, { a: MenuItem? -> onChosen?.accept(m) }, item.type, null, item.amount)
            }
            .toList()
        ui.openMenu(player, title, items)
    }

    /**
     * 指定したアイテムスタックから名前を取得します。
     * @param item 取得するアイテム
     * @return アイテムのdisplayName
     */
    private fun getItemName(item: ItemStack): String {
        val dn = item.itemMeta.displayName()
        return if (dn != null) PlainTextComponentSerializer.plainText().serialize(dn) else item.i18NDisplayName!!
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