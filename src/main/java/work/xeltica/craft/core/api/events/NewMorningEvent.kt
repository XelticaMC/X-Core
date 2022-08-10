package work.xeltica.craft.core.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.NewMorningEvent

/**
 * マイクラ内で朝が来たときに発生するイベント
 * @author Xeltica
 */
class NewMorningEvent(val time: Long) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}