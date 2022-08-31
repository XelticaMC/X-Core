package work.xeltica.craft.core.modules.counter

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerCounterStart(val player: Player, val counter: CounterData): Event() {
    companion object {
        private val HANDLERS_LIST = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }
    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }
}