package work.xeltica.craft.core.modules.kusa

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class KusaHandler : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerKusa(e: ChatEvent) {
        val message = PlainTextComponentSerializer.plainText().serialize(e.message())
        KusaModule.handleKusa(message, e.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerKusaOnChannel(e: LunaChatBukkitChannelMessageEvent) {
        val member = e.member
        if (member !is ChannelMemberPlayer) return
        KusaModule.handleKusa(e.originalMessage, member.player)
    }
}