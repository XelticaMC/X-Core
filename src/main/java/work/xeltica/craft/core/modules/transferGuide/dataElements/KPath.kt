package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

/**
 * 路線データ内の隣の駅までの経路を表すクラス
 * @author Knit prg.
 */

class KPath(conf: ConfigurationSection) {
    val to = conf.getString("to") ?: "null"
    val line = conf.getString("line") ?: "null"
    val direction = conf.getString("direction") ?: "null"
    val time = conf.getInt("time")
    fun toStringForGuide(data: TransferGuideData): String {
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        return if (line == "walk") "${white}${data.stations[to]?.name}${gray}:${data.directions[direction]}約${
            TransferGuideUtil.secondsToString(
                time
            )
        }歩く"
        else "${white}${data.stations[to]?.name}${gray}:${data.lines[line]?.name}(${data.directions[direction]}) 約${
            TransferGuideUtil.secondsToString(
                time
            )
        }"
    }
}