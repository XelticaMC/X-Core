package work.xeltica.craft.core.modules.player

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerHandler : Listener {
    @EventHandler
    fun onPlayerDeath(e: PlayerRespawnEvent) {
        val p = e.player
        if (p.world.name.startsWith("wildareab")) {
            var respawnLocation = p.bedSpawnLocation
            if (respawnLocation == null) {
                respawnLocation = Bukkit.getWorld("main")?.spawnLocation!!
            }
            e.respawnLocation = respawnLocation
        }
    }
}