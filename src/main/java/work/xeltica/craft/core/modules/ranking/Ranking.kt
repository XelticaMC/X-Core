package work.xeltica.craft.core.modules.ranking

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.utils.Time
import java.io.IOException
import java.util.*

class Ranking(val name: String, private val conf: Config) {
    private lateinit var records: HashMap<String, Int>
    private lateinit var thisSection: ConfigurationSection

    init {
        loadSection()
        loadRecords()
    }

    fun getDisplayName(): String? {
        return thisSection.getString("displayName")
    }

    fun setDisplayName(name: String) {
        setDisplayName(name, true)
    }

    fun setDisplayName(name: String, save: Boolean) {
        set("displayName", name, save)
        RankingModule.renderAll()
    }

    fun isPlayerMode(): Boolean {
        return thisSection.getBoolean("isPlayerMode")
    }

    fun setIsPlayerMode(value: Boolean) {
        setIsPlayerMode(value, true)
    }

    fun setIsPlayerMode(value: Boolean, save: Boolean) {
        set("isPlayerMode", value, save)
        RankingModule.renderAll()
    }

    fun setMode(mode: String) {
        set("mode", mode)
        RankingModule.renderAll()
    }

    fun getMode(): String? {
        return thisSection.getString("mode", "normal")
    }

    fun getHologramLocation(): Location? {
        return thisSection.getLocation("hologramLocation")
    }

    fun getHologramHidden(): Boolean {
        return thisSection.getBoolean("hologramHidden")
    }

    fun setHologram(location: Location?, isHidden: Boolean) {
        set("hologramLocation", location, false)
        set("hologramHidden", isHidden, false)
        save()
        RankingModule.renderAll()
    }

    fun save() {
        conf.save()
    }

    operator fun get(id: String): Int {
        return records[id] ?: 0
    }

    fun queryRanking(): List<RankingRecord> {
        return queryRanking(10)
    }

    fun queryRanking(count: Int): List<RankingRecord> {
        val data = modeFormatRecords()
        if (data.size > count) {
            return data.take(count)
        }
        return data
    }

    private fun modeFormatRecords(): List<RankingRecord> {
        when (getMode()) {
            "normal" -> {
                return records.entries
                    .sortedBy { it.value }
                    .reversed()
                    .map {
                        var key = it.key
                        if (isPlayerMode()) {
                            val player = Bukkit.getOfflinePlayer(UUID.fromString(key))
                            key = player.name.toString()
                        }
                        RankingRecord(key, it.value.toString())
                    }
            }
            "time" -> {
                return records.entries
                    .sortedBy { it.value }
                    .map {
                        var key = it.key
                        if (isPlayerMode()) {
                            val player = Bukkit.getOfflinePlayer(UUID.fromString(key))
                            key = player.name.toString()
                        }
                        RankingRecord(key, Time.msToString(it.value.toLong()))
                    }
            }
            "point" -> {
                return records.entries
                    .sortedBy { it.value }
                    .reversed()
                    .map {
                        var key = it.key
                        if (isPlayerMode()) {
                            val player = Bukkit.getOfflinePlayer(UUID.fromString(key))
                            key = player.name.toString()
                        }
                        RankingRecord(key, it.value.toString() + "点")
                    }
            }
            else -> throw IllegalArgumentException()
        }
    }

    fun add(id: String, score: Int) {
        records[id] = score
        saveRecords()
        RankingModule.renderAll()
    }

    fun remove(id: String) {
        records.remove(id)
        saveRecords()
        RankingModule.renderAll()
    }

    private fun set(key: String, value: Any?) {
        set(key, value, true)
    }

    private fun set(key: String, value: Any?, save: Boolean) {
        thisSection.set(key, value)
        if (save) conf.save()
    }

    private fun loadSection() {
        val s = conf.conf.getConfigurationSection(name)
        thisSection = s ?: conf.conf.createSection(name)
    }

    private fun loadRecords() {
        val recordsSection = thisSection.getConfigurationSection("records")
        records = HashMap()
        if (recordsSection == null) return
        for (key in recordsSection.getKeys(false)) {
            records[key] = recordsSection.getInt(key)
        }
    }

    private fun saveRecords() {
        val r = thisSection.getConfigurationSection("records")
        val recordsSection = r ?: thisSection.createSection("records")
        for (key in recordsSection.getKeys(false)) {
            recordsSection.set(key, null)
        }
        for (id in records.keys) {
            recordsSection.set(id, records[id])
        }

        try {
            conf.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}