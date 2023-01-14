package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.ChatColor
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

/**
 * 路線データ内の隣の駅までの経路を表すクラス
 * @author Knit prg.
 */
class KPath(val to: String?, val line: String?, val direction: String?, val time: Int?) {
    fun toStringForGuide(data: TransferGuideData): String {
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        return if (line == "walk") "${white}${data.stations[to]?.name}${gray}:${data.directions[direction]}約${
            TransferGuideUtil.secondsToString(
                time ?: -1
            )
        }歩く"
        else if (line == "boat") "${white}${data.stations[to]?.name}${gray}:${data.directions[direction]}約${
            TransferGuideUtil.secondsToString(
                time ?: -1
            )
        }ボートに乗る"
        else "${white}${data.stations[to]?.name}${gray}:${data.lines[line]?.name}(${data.directions[direction]}) 約${
            TransferGuideUtil.secondsToString(
                time ?: -1
            )
        }"
    }
}