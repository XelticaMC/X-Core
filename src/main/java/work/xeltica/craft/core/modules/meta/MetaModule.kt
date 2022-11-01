package work.xeltica.craft.core.modules.meta

import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.io.IOException

object MetaModule: ModuleBase() {
    val changeLog = listOf("モジュール化").toTypedArray()

    lateinit var meta: Config
    val currentVersion = XCorePlugin.instance.description.version
    var previousVersion: String? = null; private set
    var isUpdated = false; private set
    var postToDiscord = false; private set

    override fun onEnable() {
        meta = Config("meta")
        checkUpdate()
    }

    private fun checkUpdate() {
        val conf: YamlConfiguration = meta.conf
        val confCurrentVersion = conf.getString("version", null)
        postToDiscord = conf.getBoolean("postToDiscord", false)
        if (!conf.contains("postToDiscord")) {
            conf["postToDiscord"] = false
        }
        previousVersion = conf.getString("previousVersion", null)
        if (confCurrentVersion == null || confCurrentVersion != currentVersion) {
            conf["version"] = currentVersion
            conf["previousVersion"] = confCurrentVersion
            previousVersion = confCurrentVersion
            isUpdated = true
            try {
                meta.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}