package work.xeltica.craft.core.modules.eventTwoYears

import org.bukkit.Location
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.counter.CounterModule

object EventTwoYearsModule : ModuleBase() {
    const val PS_KEY_CHECKPOINT = "2yearsParkourGameCheckpoint"
    const val PS_KEY_DEATH_COUNT = "2yearsParkourGameDeathCount"
    const val EVENT_COUNTER_ID = "2yearsparkour"
    override fun onEnable() {
        registerHandler(EventTwoYearsHandler())
    }

    fun setCheckpoint(player: Player, location: Location) {
        val store = PlayerStore.open(player)
        store[PS_KEY_CHECKPOINT] = location
    }

    fun getCheckPoint(player: Player): Location {
        val counterStartLocation = CounterModule[EVENT_COUNTER_ID]?.location1 ?: throw Exception("カウンター $EVENT_COUNTER_ID が見つからない")

        return PlayerStore.open(player).getLocation(PS_KEY_CHECKPOINT, counterStartLocation) ?: throw Exception("BUG")
    }

    fun incrementDeathCount(player: Player) {
        val store = PlayerStore.open(player)
        store[PS_KEY_DEATH_COUNT] = store.getInt(PS_KEY_DEATH_COUNT, 0) + 1
    }

    fun resetPlayerStore(player: Player) {
        val store = PlayerStore.open(player)
        store.delete(PS_KEY_CHECKPOINT)
        store.delete(PS_KEY_DEATH_COUNT)
    }
}

