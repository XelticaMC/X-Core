package work.xeltica.craft.core.api

import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import kotlin.jvm.JvmOverloads
import kotlin.Throws
import java.io.IOException
import java.io.File
import java.util.function.Consumer

/**
 * Spigot の設定ファイル機能を扱いやすいように、面倒な部分をラッピングしています。
 *
 * @author Xeltica
 */
class Config @JvmOverloads constructor(val configName: String, private val onReloaded: Consumer<Config>? = null) {
    fun reload() {
        conf = YamlConfiguration.loadConfiguration(openFile(configName))
        onReloaded?.accept(this)
    }

    @Throws(IOException::class)
    fun save() {
        conf.save(openFile(configName))
        reload()
    }

    lateinit var conf: YamlConfiguration
        private set

    init {
        reload()
    }

    companion object {
        fun exists(configName: String): Boolean {
            return openFile(configName).exists()
        }

        fun delete(configName: String): Boolean {
            return openFile(configName).delete()
        }

        private fun openFile(configName: String): File {
            val folder = instance.dataFolder
            return File(folder, "$configName.yml")
        }
    }
}