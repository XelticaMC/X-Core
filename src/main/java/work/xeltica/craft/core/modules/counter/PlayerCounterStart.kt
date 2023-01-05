package work.xeltica.craft.core.modules.counter

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * プレイヤーがタイムアタック カウンターを作動させたときに発生します。
 */
class PlayerCounterStart(val player: Player, val counter: CounterData) : Event() {
    companion object {
        private val HANDLERS_LIST = HandlerList()

        // NOTE: Spigotは動的にこの関数を呼び出すため、JvmStaticでなければならない
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }
}