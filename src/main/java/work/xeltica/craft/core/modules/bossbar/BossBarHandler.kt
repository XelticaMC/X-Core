package work.xeltica.craft.core.modules.bossbar

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BossBarHandler: Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        BossBarModule.applyAll(e.player)
    }
}