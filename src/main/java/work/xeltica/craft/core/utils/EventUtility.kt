package work.xeltica.craft.core.utils

import java.time.LocalDate

object EventUtility {
    @JvmStatic
    fun isEventNow(): Boolean {
        val today = LocalDate.now()
        val startEventDay = LocalDate.of(2022, 8, 16)
        val endEventDay = LocalDate.of(2022, 9, 1)
        return today.isAfter(startEventDay) && today.isBefore(endEventDay)
    }
}