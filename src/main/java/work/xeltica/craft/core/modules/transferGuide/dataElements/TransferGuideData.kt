package work.xeltica.craft.core.modules.transferGuide.dataElements

import org.bukkit.configuration.ConfigurationSection
import work.xeltica.craft.core.api.Config
import java.time.LocalDateTime

/**
 * 路線データを表すクラス
 * @author Knit prg.
 */

class TransferGuideData {
    val stations: Map<String, KStation>
    val lines: Map<String, KLine>
    val directions: Map<String, String>
    val companies: Map<String, KCompany>
    val municipalities: Map<String, KMuni>
    val loopMax: Int
    val update: LocalDateTime
    val consoleDebug: Boolean

    init {
        val conf = Config("transferGuideData").conf
        stations = stationsConfigToKStations(conf.getConfigurationSection("stations"))
        lines = linesConfigToKLines(conf.getConfigurationSection("lines"))
        directions = pairStringConfigToMap(conf.getConfigurationSection("directions"))
        companies = companiesConfigToKCompanies(conf.getConfigurationSection("companies"))
        municipalities = munisConfigToKMunis(conf.getConfigurationSection("municipalities"))
        loopMax = conf.getInt("loopMax")
        update = LocalDateTime.parse(conf.getString("update"))
        consoleDebug = conf.getBoolean("consoleDebug", false)
    }

    fun stationExists(stationId: String): Boolean {
        return stations[stationId] != null
    }

    fun isStationInWorld(stationId: String, worldName: String): Boolean {
        return stations[stationId]?.world == worldName
    }

    fun getStationsInWorld(worldName: String): Set<KStation> {
        return stations.filter { it.value.world == worldName }.values.toSet()
    }

    fun lineExists(lineId: String): Boolean {
        return lines[lineId] != null
    }

    fun directionExists(directionId: String): Boolean {
        return directions[directionId] != null
    }

    private fun stationsConfigToKStations(conf: ConfigurationSection?): Map<String, KStation> {
        val map = mutableMapOf<String, KStation>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KStation(this, key) }
        }
        return map.toMap()
    }

    private fun linesConfigToKLines(conf: ConfigurationSection?): Map<String, KLine> {
        val map = mutableMapOf<String, KLine>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KLine(this) }
        }
        return map
    }

    private fun companiesConfigToKCompanies(conf: ConfigurationSection?): Map<String, KCompany> {
        val map = mutableMapOf<String, KCompany>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KCompany(this) }
        }
        return map.toMap()
    }

    private fun munisConfigToKMunis(conf: ConfigurationSection?): Map<String, KMuni> {
        val map = mutableMapOf<String, KMuni>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KMuni(this, key) }
        }
        return map.toMap()
    }

    private fun pairStringConfigToMap(conf: ConfigurationSection?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        conf ?: return map
        conf.getKeys(false).forEach {
            conf.getString(it)?.run { map[it] = this }
        }
        return map.toMap()
    }
}