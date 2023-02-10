package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

/**
 * 路線データ内の自治体を表すクラス
 * @author Knit prg.
 */
class KMuni(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name")
    val world = conf.getString("world")
    val stations: List<String> = conf.getStringList("stations")
}