package work.xeltica.craft.core.modules.transferGuide

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * TransferGuideでよく使う関数群です。
 * @author Knit prg.
 */
object TransferGuideUtil {

    /**
     * 距離算出
     */
    fun calcDistance(start: DoubleArray, end: DoubleArray): Double {
        return abs(sqrt((start[0] - end[0]).pow(2.0) + (start[1] - end[1]).pow(2.0)))
    }

    /**
     * メートルを表すDouble値をいい感じの文字列に変換します。
     */
    fun metersToString(meter: Double): String {
        return if (meter >= 1000) "%.1fキロメートル".format(meter / 1000)
        else "%.1fメートル".format(meter)
    }

    /**
     * 秒を表すInt値をいい感じの文字列に変換します。
     */
    fun secondsToString(seconds: Int): String {
        return if (seconds >= 60 && seconds % 60 == 0) "${seconds / 60}分"
        else if (seconds >= 60) "${seconds / 60}分${seconds % 60}秒"
        else "${seconds}秒"
    }
}