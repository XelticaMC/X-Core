package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent

/**
 * 現実時間で次の日になったイベント
 * @author Xeltica
 */
class RealTimeNewDayEvent : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}