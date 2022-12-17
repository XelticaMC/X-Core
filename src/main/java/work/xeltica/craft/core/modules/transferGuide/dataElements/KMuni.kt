package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

class KMuni(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name") ?: "null"
    val world = conf.getString("world")
    val stations: List<String> = conf.getStringList("stations")
}