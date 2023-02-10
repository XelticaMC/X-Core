package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection

/**
 * 路線データ内の鉄道会社を表すクラス
 * @author Knit prg.
 */
class KCompany(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name")
    val lines: List<String> = conf.getStringList("lines")
}