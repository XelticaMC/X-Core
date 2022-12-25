package work.xeltica.craft.core.utils

import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import java.io.File
import java.io.IOException
import java.util.function.Consumer

/**
 * Spigot の設定ファイル機能を扱いやすいように、面倒な部分をラッピングしています。
 * @author Xeltica
 */
class Config @JvmOverloads constructor(val configName: String, private val onReloaded: Consumer<Config>? = null) {
    fun reload() {
        conf = YamlConfiguration.loadConfiguration(openFile())
        onReloaded?.accept(this)
    }

    @Throws(IOException::class)
    fun save() {
        conf.save(openFile())
        reload()
    }

    lateinit var conf: YamlConfiguration
        private set

    init {
        reload()
    }

    private fun openFile(): File {
        return openFile(configName)
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