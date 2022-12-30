package work.xeltica.craft.core.modules.motd

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.hub.HubType
import work.xeltica.craft.core.modules.player.PlayerDataKey

class MotdHandler : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())

        e.joinMessage(Component.text("§a$name§bさんがやってきました"))
        if (!e.player.hasPlayedBefore()) {
            e.joinMessage(Component.text("§a$name§bさんが§6§l初参加§rです"))
            PlayerStore.open(e.player)[PlayerDataKey.NEWCOMER_TIME] = defaultNewComerTime
            HubModule.teleport(e.player, HubType.NewComer, true)
        }

        e.player.showTitle(
            Title.title(
            Component.text("§aXelticaMC"),
            Component.text("§f§b§nhttps://craft.xeltica.work")
        ))

        HintModule.achieve(e.player, Hint.WELCOME)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        e.quitMessage(Component.text("§a$name§bさんがかえりました"))
    }
    // 30分
    private val defaultNewComerTime = 20 * 60 * 30
}