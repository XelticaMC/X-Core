package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

/**
 * 路線データ内や経路内での駅を表すクラス
 * @author Knit prg.
 */
class KStation(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name")
    val yomi = conf.getString("yomi")
    val number = conf.getString("number")
    val world = conf.getString("world")
    val location = conf.getDoubleList("location").toDoubleArray()
    val type = conf.getString("type")
    val paths = pathsConfigToKPaths(conf.getList("paths"))
    private fun pathsConfigToKPaths(conf: List<*>?): Array<KPath> {
        val set = arrayListOf<KPath>()
        conf ?: return set.toTypedArray()
        for (i in conf.indices) {
            val item = conf[i]
            if (item !is java.util.LinkedHashMap<*, *>) continue
            val toT = item["to"]
            val lineT = item["line"]
            val directionT = item["direction"]
            val timeT = item["time"]
            val to = if (toT is String) {
                toT
            } else {
                null
            }
            val line = if (lineT is String) {
                lineT
            } else {
                null
            }
            val direction = if (directionT is String) {
                directionT
            } else {
                null
            }
            val time = if (timeT is Int) {
                timeT
            } else {
                null
            }
            set.add(
                KPath(to, line, direction, time)
            )
        }
        return set.toTypedArray()
    }
}