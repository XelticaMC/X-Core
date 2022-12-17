package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

class KStation(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name") ?: "null"
    val yomi = conf.getString("yomi") ?: "null"
    val number = conf.getString("number")
    val world = conf.getString("world") ?: "null"
    val location = conf.getDoubleList("location").toDoubleArray()
    val paths = pathsConfigToKPaths(conf.getConfigurationSection("paths"))
    private fun pathsConfigToKPaths(conf: ConfigurationSection?): MutableSet<KPath> {
        val set = mutableSetOf<KPath>()
        conf ?: return set
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { set.add(KPath(this)) }
        }
        return set
    }
}