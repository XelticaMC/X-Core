package work.xeltica.craft.core.modules.transferPlayerData

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TransferPlayerDataEvent(val from: Player, val to: Player) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }

    companion object {
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }

        @JvmStatic
        private val HANDLERS_LIST = HandlerList()
    }
}
