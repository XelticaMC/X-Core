package work.xeltica.craft.core.api.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.PlayerCounterStart
import work.xeltica.craft.core.models.CounterData

/**
 * プレイヤーが時間計測を開始したイベント
 * @author Xeltica
 */
class PlayerCounterStart(val player: Player, val counter: CounterData) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}