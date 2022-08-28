package work.xeltica.craft.core.modules.quickchat

import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.stores.HintStore

class QuickChatHandler: Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuickChat(e: ChatEvent) {
        val component = e.message() as? TextComponent ?: return
        var msg = component.content()
        val player = e.player
        if (!msg.startsWith(".")) return
        msg = msg.substring(1)
        if (QuickChatModule.getAllPrefix().contains(msg)) {
            var quickMsg = QuickChatModule.getMessage(msg)
            quickMsg = QuickChatModule.chatFormat(quickMsg, player)
            e.message(Component.text(quickMsg))
            HintStore.instance.achieve(player, Hint.QUICKCHAT)
        }
    }
}