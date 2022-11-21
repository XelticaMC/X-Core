package work.xeltica.craft.core.modules.player

import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.UUID

class PlayerRecord(private val config: Config, private val section: ConfigurationSection, val playerId: UUID) {
    fun set(key: PlayerDataKey, value: Any?) {
        if (value == get(key)) return
        section.set(key.physicalKey, value)
    }

    fun get(key: PlayerDataKey, defaultValue: Any? = null): Any? {
        return section.get(key.physicalKey, defaultValue)
    }

    fun getString(key: PlayerDataKey, defaultValue: String? = null): String? {
        return section.getString(key.physicalKey, defaultValue)
    }

    fun isString(key: PlayerDataKey): Boolean {
        return section.isString(key.physicalKey)
    }

    fun getStringList(key: PlayerDataKey): List<String> {
        return section.getStringList(key.physicalKey)
    }

    fun isList(key: PlayerDataKey): Boolean {
        return section.isList(key.physicalKey)
    }

    fun getInt(key: PlayerDataKey, defaultValue: Int = 0): Int {
        return section.getInt(key.physicalKey, defaultValue)
    }

    fun isInt(key: PlayerDataKey): Boolean {
        return section.isInt(key.physicalKey)
    }

    fun getBoolean(key: PlayerDataKey, defaultValue: Boolean = false): Boolean {
        return section.getBoolean(key.physicalKey, defaultValue)
    }

    fun isBoolean(key: PlayerDataKey): Boolean {
        return section.isBoolean(key.physicalKey)
    }

    fun getDouble(key: PlayerDataKey, defaultValue: Double = 0.0): Double {
        return section.getDouble(key.physicalKey, defaultValue)
    }

    fun isDouble(key: PlayerDataKey): Boolean {
        return section.isDouble(key.physicalKey)
    }

    fun getLong(key: PlayerDataKey, defaultValue: Long = 1): Long {
        return section.getLong(key.physicalKey, defaultValue)
    }

    fun isLong(key: PlayerDataKey): Boolean {
        return section.isLong(key.physicalKey)
    }

    fun getVector(key: PlayerDataKey, defaultValue: Vector? = null): Vector? {
        return section.getVector(key.physicalKey, defaultValue)
    }

    fun isVector(key: PlayerDataKey): Boolean {
        return section.isVector(key.physicalKey)
    }

    fun getLocation(key: PlayerDataKey, defaultValue: Location? = null): Location? {
        return section.getLocation(key.physicalKey, defaultValue)
    }

    fun isLocation(key: PlayerDataKey): Boolean {
        return section.isLocation(key.physicalKey)
    }

    fun delete(key: PlayerDataKey) {
        set(key, null)
    }

    fun save() {
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}