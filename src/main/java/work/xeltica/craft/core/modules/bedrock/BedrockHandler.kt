package work.xeltica.craft.core.modules.bedrock

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore

class BedrockHandler : Listener {
    @EventHandler
    fun onBedrockPlayerJoin(e: PlayerJoinEvent) {
        val record = PlayerStore.open(e.player)
        if (record.getBoolean(BedrockModule.PS_KEY_ACCEPT_DISCLAIMER)) return

        BedrockModule.showDisclaimerAsync(e.player)
    }
}