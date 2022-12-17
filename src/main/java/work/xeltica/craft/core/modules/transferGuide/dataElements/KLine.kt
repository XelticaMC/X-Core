package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

class KLine(conf: ConfigurationSection) {
    val name = conf.getString("name") ?: "null"
    val world=conf.getString("world")?:"null"
    val stations: List<String> = conf.getStringList("stations")
}