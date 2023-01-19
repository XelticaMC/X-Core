package work.xeltica.craft.core.modules.clover

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataEvent

class CloverHandler : Listener {
    @EventHandler
    fun onTransferPlayerData(e: TransferPlayerDataEvent) {
        val hasClover = CloverModule.getCloverOf(e.from)
        CloverModule[e.to] = hasClover
        CloverModule.delete(e.from)
    }
}