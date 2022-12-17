package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

class KPath(conf: ConfigurationSection) {
    val to = conf.getString("to") ?: "null"
    val line = conf.getString("line") ?: "null"
    val direction = conf.getString("direction") ?: "null"
    val time = conf.getInt("time")
    fun toStringForGuide(data: TransferGuideData): String {
        return if (line == "walk") "${data.stations[to]?.name}:${data.directions[direction]}約${
            TransferGuideUtil.secondsToString(
                time
            )
        }歩く"
        else "${data.stations[to]?.name}:${data.lines[line]?.name}(${data.directions[direction]}) 約${
            TransferGuideUtil.secondsToString(
                time
            )
        }"
    }
}