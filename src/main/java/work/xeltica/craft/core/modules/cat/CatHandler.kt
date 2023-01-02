package work.xeltica.craft.core.modules.cat

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import work.xeltica.craft.core.api.playerStore.PlayerStore

class CatHandler : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: ChatEvent) {
        if (PlayerStore.open(e.player).getBoolean(CatModule.keyIsCat)) {
            val text = e.message() as TextComponent
            e.message(Component.text(CatModule.nyaize(text.content())))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: LunaChatBukkitChannelMessageEvent) {
        val member = e.member
        if (member !is ChannelMemberPlayer) return
        if (PlayerStore.open(member.player).getBoolean(CatModule.keyIsCat))
            e.message = CatModule.nyaize(e.message)
    }
}