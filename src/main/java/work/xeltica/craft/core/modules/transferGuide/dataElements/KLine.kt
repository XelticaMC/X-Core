package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

/**
 * 路線データ内の路線を表すクラス
 * @author Knit prg.
 */
class KLine(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name")
    val world = conf.getString("world")
    val rapid = conf.getBoolean("rapid")
    val stations: List<String> = conf.getStringList("stations")
}