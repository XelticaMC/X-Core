package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.ChatColor
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

/**
 * 路線データ内の隣の駅までの経路を表すクラス
 * @author Knit prg.
 */
class KPath(val to: String?, val line: String?, val direction: String?, val time: Int?, val rapidNotInParallel: Boolean?) {
    /**
     * 駅情報表示で隣の駅までの経路を表示するときに呼ばれる
     * 鉄道なら「駅名:路線名(方向名) 約○分○秒」
     * 徒歩などなら「駅名:方向名約○分○秒歩く/ボートに乗る」
     */
    fun toStringForGuide(data: TransferGuideData): String {
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        val stationName = data.stations[to]?.name
        val lineName = data.lines[line]?.name
        val directionName = data.directions[direction]
        val time = TransferGuideUtil.secondsToString(time ?: -1)
        return when (line) {
            "walk" -> "${white}${stationName}${gray}:${directionName}約${time}歩く"
            "boat" -> "${white}${stationName}${gray}:${directionName}約${time}ボートに乗る"
            else -> "${white}${stationName}${gray}:${lineName}(${directionName}) 約${time}"
        }
    }
}