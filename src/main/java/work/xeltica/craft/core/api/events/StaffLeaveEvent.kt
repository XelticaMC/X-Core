package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.StaffLeaveEvent

/**
 * スタッフがサーバーから退出したときに発生するイベント
 * @author Xeltica
 */
class StaffLeaveEvent : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}