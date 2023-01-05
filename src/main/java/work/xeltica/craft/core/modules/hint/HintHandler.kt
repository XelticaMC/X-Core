package work.xeltica.craft.core.modules.hint

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataEvent

class HintHandler : Listener {
    @EventHandler
    fun onTransferPlayerData(e: TransferPlayerDataEvent) {
        for (hintName in HintModule.getAllAchievedHintNames(e.from)) {
            for (hint in Hint.values()) {
                if (hint.hintName == hintName) {
                    HintModule.achieve(e.to, hint, false)
                    break
                }
            }
        }
        HintModule.clearHints(e.from)
    }
}