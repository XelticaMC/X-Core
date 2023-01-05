package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 現実時間で次の日になったイベント
 * @author Lutica
 */
class RealTimeNewDayEvent : Event() {
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