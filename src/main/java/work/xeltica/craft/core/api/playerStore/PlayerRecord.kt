package work.xeltica.craft.core.api.playerStore

import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

class PlayerRecord(private val section: ConfigurationSection) {
    operator fun set(key: String, value: Any?) {
        if (value == get(key)) return
        section.set(key, value)
    }

    @JvmOverloads
    operator fun get(key: String, defaultValue: Any? = null): Any? {
        return section.get(key, defaultValue)
    }

    fun has(key: String): Boolean {
        return section.contains(key)
    }

    @JvmOverloads
    fun getString(key: String, defaultValue: String = ""): String {
        return section.getString(key, defaultValue)!!
    }

    fun isString(key: String): Boolean {
        return section.isString(key)
    }

    fun getStringList(key: String): MutableList<String> {
        return section.getStringList(key)
    }

    fun isList(key: String): Boolean {
        return section.isList(key)
    }

    @JvmOverloads
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return section.getInt(key, defaultValue)
    }

    fun isInt(key: String): Boolean {
        return section.isInt(key)
    }

    @JvmOverloads
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return section.getBoolean(key, defaultValue)
    }

    fun isBoolean(key: String): Boolean {
        return section.isBoolean(key)
    }

    @JvmOverloads
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return section.getDouble(key, defaultValue)
    }

    fun isDouble(key: String): Boolean {
        return section.isDouble(key)
    }

    @JvmOverloads
    fun getLong(key: String, defaultValue: Long = 1): Long {
        return section.getLong(key, defaultValue)
    }

    fun isLong(key: String): Boolean {
        return section.isLong(key)
    }

    @JvmOverloads
    fun getVector(key: String, defaultValue: Vector? = null): Vector? {
        return section.getVector(key, defaultValue)
    }

    fun isVector(key: String): Boolean {
        return section.isVector(key)
    }

    @JvmOverloads
    fun getLocation(key: String, defaultValue: Location? = null): Location? {
        return section.getLocation(key, defaultValue)
    }

    fun isLocation(key: String): Boolean {
        return section.isLocation(key)
    }

    fun delete(key: String) {
        set(key, null)
    }

    fun getAll(): Map<String, Any?> {
        return section.getKeys(false).associateWith { section[it] }
    }
}