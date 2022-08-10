package work.xeltica.craft.core.api.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import work.xeltica.craft.core.api.events.PlayerCounterFinish
import work.xeltica.craft.core.models.CounterData

/**
 * プレイヤーが時間計測を停止したイベント
 * @author Xeltica
 */
class PlayerCounterFinish(val player: Player, val counter: CounterData, val time: Long) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}