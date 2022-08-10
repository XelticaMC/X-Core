package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.StaffJoinEvent

/**
 * スタッフがサーバーに参加したときに発生するイベント
 * @author Xeltica
 */
class StaffJoinEvent : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}