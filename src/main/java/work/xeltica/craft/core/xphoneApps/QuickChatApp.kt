package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.UIModule
import work.xeltica.craft.core.models.MenuItem
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.modules.HintModule
import work.xeltica.craft.core.modules.QuickChatModule

/**
 * クイックチャットアプリ
 * @author raink1208
 */
class QuickChatApp : AppBase() {
    override fun getName(player: Player): String = "クイックチャット"

    override fun getIcon(player: Player): Material = Material.PAPER

    override fun onLaunch(player: Player) {
        val list = ArrayList<MenuItem>()
        val ui = UIModule.getInstance()
        ui.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, SoundPitch.F_1)

        for (chat in QuickChatModule.allPrefix) {
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