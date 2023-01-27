package work.xeltica.craft.core.modules.moderation

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

class ModerationHandler : Listener {
    @EventHandler
    fun onPlayerGameModeChange(e: PlayerGameModeChangeEvent) {
        if (e.newGameMode == GameMode.SPECTATOR) {
            e.player.performCommand("dynmap hide")
        } else if (e.player.hasPermission("dynmap.show")) {
            e.player.performCommand("dynmap show")
        }
    }
}