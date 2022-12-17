package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

class KCompany(conf: ConfigurationSection) {
    val name = conf.getString("name") ?: "null"
    val lines: List<String> = conf.getStringList("lines")
}