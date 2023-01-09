package work.xeltica.craft.core.modules.transferGuide.routeElements

import org.bukkit.ChatColor
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData

/**
 * 経路データ内の移動を表すクラス。
 */

class KRoutePathReal(
    val line: String,
    val direction: String,
    var time: Int,
) : KRoutePath() {
    fun toStringForGuide(data: TransferGuideData): String {
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        return if (line == "walk") {
            "${white}${data.directions[direction]}歩く ${gray}約${TransferGuideUtil.secondsToString(time)}"
        } else {
            "${white}${data.lines[line]?.name}(${data.directions[direction]}) ${gray}約${
                TransferGuideUtil.secondsToString(
                    time
                )
            }"
        }
    }
}