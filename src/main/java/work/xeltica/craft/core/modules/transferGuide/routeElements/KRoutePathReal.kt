package work.xeltica.craft.core.modules.transferGuide.routeElements

import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData

class KRoutePathReal(
    val line: String,
    val direction: String,
    var time: Int,
) : KRoutePath() {
    fun toStringForGuide(data: TransferGuideData): String {
        return if (line == "walk") "${data.directions[direction]}約${TransferGuideUtil.secondsToString(time)}歩く"
        else "${data.lines[line]?.name}(${data.directions[direction]}) 約${TransferGuideUtil.secondsToString(time)}"
    }
}