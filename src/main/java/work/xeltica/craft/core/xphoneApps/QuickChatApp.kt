package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.MenuItem
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.stores.HintStore
import work.xeltica.craft.core.stores.QuickChatStore

/**
 * クイックチャットアプリ
 * @author raink1208
 */
class QuickChatApp : AppBase() {
    override fun getName(player: Player): String = "クイックチャット"

    override fun getIcon(player: Player): Material = Material.PAPER

    override fun onLaunch(player: Player) {
        val store = QuickChatStore.instance
        val list = ArrayList<MenuItem>()
        val ui = Gui.getInstance()
        ui.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, SoundPitch.F_1)

        for (chat in store.allPrefix) {
            val msg = store.chatFormat(store.getMessage(chat), player)
            list.add(
                MenuItem(String.format("%s §7(.%s)", msg, chat), {
                    player.chat(msg)
                    HintStore.instance.achieve(player, Hint.QUICKCHAT_APP)
                }, Material.PAPER)
            )
        }
        ui.openMenu(player, "クイックチャット", list)
    }
}