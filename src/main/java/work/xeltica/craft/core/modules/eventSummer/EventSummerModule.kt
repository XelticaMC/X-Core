package work.xeltica.craft.core.modules.eventSummer

import work.xeltica.craft.core.api.ModuleBase
import java.time.LocalDate

object EventSummerModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(EventSummerHandler())
    }

    @JvmStatic
    fun isEventNow(): Boolean {
        val today = LocalDate.now()
        val startEventDay = LocalDate.of(2022, 8, 15)
        val endEventDay = LocalDate.of(2022, 9, 1)
        return today.isAfter(startEventDay) && today.isBefore(endEventDay)
    }
}