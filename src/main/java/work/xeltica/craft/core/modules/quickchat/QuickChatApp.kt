package work.xeltica.craft.core.modules.quickchat

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.gui.SoundPitch
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * クイックチャットアプリ
 * @author raink1208
 */
class QuickChatApp : AppBase() {
    override fun getName(player: Player): String = "クイックチャット"

    override fun getIcon(player: Player): Material = Material.PAPER

    override fun onLaunch(player: Player) {
        val list = ArrayList<MenuItem>()
        val ui = Gui.getInstance()
        ui.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, SoundPitch.F_1)

        for (chat in QuickChatModule.getAllPrefix()) {
            val msg = QuickChatModule.chatFormat(QuickChatModule.getMessage(chat), player)
            list.add(
                MenuItem(String.format("%s §7(.%s)", msg, chat), {
                    player.chat(msg)
                    HintModule.achieve(player, Hint.QUICKCHAT_APP)
                }, Material.PAPER)
            )
        }
        ui.openMenu(player, "クイックチャット", list)
    }
}