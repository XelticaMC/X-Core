package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NewMorningEvent(val time: Long) : Event() {
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