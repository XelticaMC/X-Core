package work.xeltica.craft.core.modules.transferGuide

import work.xeltica.craft.core.modules.transferGuide.dataElements.KPath
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
     * 同じ路線のKPathを含むかどうかを判定します。
     */
    fun containsPathWithSameLine(pathsA: Array<KPath>, pathsB: Array<KPath>): Boolean {
        pathsA.forEach { pathA ->
            pathsB.forEach { pathB ->
                if (pathA.line == pathB.line) return true
            }
        }
        return false
    }

    /**
     * 同じ路線と方向のKPathを含むかどうかを判定します。
     */
    fun containsSamePath(pathsA: Array<KPath>, pathsB: Array<KPath>): Boolean {
        pathsA.forEach { pathA ->
            pathsB.forEach { pathB ->
                if (pathA.line == pathB.line && pathA.direction == pathB.direction) return true
            }
        }
        return false
    }

    /**
     * 同じ路線のKPathの路線IDを抽出します。
     */
    fun getPathWithSameLine(pathsA: Array<KPath>, pathsB: Array<KPath>): String? {
        pathsA.forEach { pathA ->
            pathsB.forEach { pathB ->
                if (pathA.line == pathB.line) return pathA.line
            }
        }
        return null
    }

    /**
     * 同じ路線と方向のKPathの路線IDと方向IDを抽出します。firstが路線、secondが方向です。
     */
    fun getSamePath(pathsA: Array<KPath>, pathsB: Array<KPath>): Pair<String?, String?>? {
        pathsA.forEach { pathA ->
            pathsB.forEach { pathB ->
                if (pathA.line == pathB.line && pathA.direction == pathB.direction) return Pair(pathA.line, pathA.direction)
            }
        }
        return null
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