package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.stores.PlayerStore
import java.util.*

/**
 * 祭り用花火購入・入手アプリ
 * @author Ebise Lutica
 */
class FireworkApp : AppBase() {
    private val fireworkCount = 16
    private val fireworkCost = 100

    override fun getName(player: Player): String =
        if (isBonusReceived(player))
            "花火を購入（${fireworkCost}EP/${fireworkCount}個）"
        else
            "花火を引き換える"

    override fun getIcon(player: Player): Material = Material.FIREWORK_ROCKET

    override fun onLaunch(player: Player) {
        val bonusReceived = isBonusReceived(player)
        val verb = if (bonusReceived) "購入" else "入手"
        val ui = Gui.getInstance()
        if (bonusReceived && !EbiPowerStore.getInstance().tryTake(player, 80)) {
            ui.error(player, "アイテムを${verb}できませんでした。エビパワーが足りません。")
            return
        }
        val stack = PlayerStore.getInstance().getRandomFireworkByUUID(player.uniqueId, fireworkCount)
        val size = player.inventory.addItem(stack).size
        if (size > 0) {
            ui.error(player, "アイテムを${verb}できませんでした。持ち物がいっぱいです。整理してからもう一度お試し下さい。")
            if (size - fireworkCount > 0) {
                val stackToRemove = stack.clone()
                stackToRemove.amount = size - fireworkCount
                player.inventory.remove(stackToRemove)
            }
            EbiPowerStore.getInstance().tryGive(player, fireworkCost)
        } else {
            player.sendMessage("花火を${verb}しました！")
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
        }
        if (!bonusReceived) {
            PlayerStore.getInstance().open(player)[PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER] = true
        }
    }

    override fun isShiny(player: Player): Boolean = !isBonusReceived(player)

    override fun isVisible(player: Player): Boolean {
        val date = Calendar.getInstance()
        val month = date[Calendar.MONTH] + 1
        val day = date[Calendar.DATE]
        return month == 5 && 15 <= day && day <= 21
    }

    private fun isBonusReceived(player: Player) = PlayerStore.getInstance()
        .open(player).getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER)
}