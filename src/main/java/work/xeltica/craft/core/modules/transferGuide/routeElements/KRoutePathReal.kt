package work.xeltica.craft.core.modules.transferGuide.routeElements

import org.bukkit.ChatColor
import work.xeltica.craft.core.modules.transferGuide.TransferGuideModule
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

/**
 * 経路データ内の移動を表すクラス。
 */
class KRoutePathReal(
    val line: String,
    val direction: String,
    var time: Int,
) : KRoutePath() {
    fun toStringForGuide(): String {
        val data = TransferGuideModule.data
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        val directionS = data.directions[direction]
        val timeS = TransferGuideUtil.secondsToString(time)
        return when (line) {
            "walk" -> "${white}${directionS}歩く ${gray}約${timeS}"
            "boat" -> "${white}${directionS}ボートに乗る ${gray}約${timeS}"
            else -> "${white}${data.lines[line]?.name}(${directionS} ${gray}約${timeS}"
        }
    }
}