package work.xeltica.craft.core.modules.transferGuide

import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData
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
     * 特定の駅へのステップ数を計算します。
     */
    fun calcStepsTo(data: TransferGuideData, destination: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val stations = data.stations.toMutableMap()
        map[destination] = 0
        stations.remove(destination)
        var i = 1
        while (i < data.loopMax && stations.isNotEmpty()) {
            val beforeStepStations = map.filter { it.value == i - 1 }
            beforeStepStations.forEach { beforeStepStation ->
                data.stations[beforeStepStation.key]?.paths?.forEach { path ->
                    val pathTo = stations[path.to]
                    if (pathTo != null) {
                        map[pathTo.id] = i
                        stations.remove(path.to)
                    }
                }
            }
            i++
        }
        return map
    }

    /**
     * 共通する路線を抽出します。
     */
    fun getSameLines(data: TransferGuideData, startId: String, endId: String): Array<String> {
        return data.lines.filter { it.value.stations.containsAll(setOf(startId, endId)) }.keys.toTypedArray()
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