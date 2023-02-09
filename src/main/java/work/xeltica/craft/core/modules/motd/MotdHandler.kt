package work.xeltica.craft.core.modules.motd

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.hub.HubType
import work.xeltica.craft.core.modules.promotion.PromotionModule
import work.xeltica.craft.core.utils.Ticks

class MotdHandler : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())

        e.joinMessage(Component.text("${ChatColor.GREEN}${name}${ChatColor.AQUA}さんがやってきました"))
        if (!e.player.hasPlayedBefore()) {
            e.joinMessage(Component.text("${ChatColor.GREEN}${name}${ChatColor.AQUA}さんが${ChatColor.GOLD}${ChatColor.BOLD}初参加${ChatColor.RESET}です"))
            PlayerStore.open(e.player)[PromotionModule.PS_KEY_NEWCOMER_TIME] = defaultNewComerTime
            HubModule.teleport(e.player, HubType.NewComer, true)
        }

        e.player.showTitle(
            Title.title(
                Component.text("${ChatColor.GREEN}XelticaMC"),
                Component.text("${ChatColor.WHITE}${ChatColor.AQUA}${ChatColor.UNDERLINE}https://craft.xeltica.work")
            )
        )

        HintModule.achieve(e.player, Hint.WELCOME)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        e.quitMessage(Component.text("${ChatColor.GREEN}${name}${ChatColor.AQUA}さんがかえりました"))
    }

    // 30分
    private val defaultNewComerTime = Ticks.from(30, 0.0)
}