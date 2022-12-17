package work.xeltica.craft.core.modules.transferGuide

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object TransferGuideUtil {
    @JvmStatic
    fun calcDistance(start: DoubleArray, end: DoubleArray): Double {
        return abs(sqrt((start[0] - end[0]).pow(2.0) + (start[1] - end[1]).pow(2.0)))
    }

    @JvmStatic
    fun metersToString(meter: Double): String {
        return if (meter >= 1000) "%.1fキロメートル".format(meter / 1000)
        else "%.1fメートル".format(meter)
    }

    @JvmStatic
    fun secondsToString(seconds: Int): String {
        return if (seconds >= 60 && seconds % 60 == 0) "${seconds / 60}分"
        else if (seconds >= 60) "${seconds / 60}分${seconds % 60}秒"
        else "${seconds}秒"
    }
}