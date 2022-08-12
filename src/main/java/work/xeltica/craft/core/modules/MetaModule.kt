package work.xeltica.craft.core.modules

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import java.io.IOException

/**
 * プラグインのメタ情報を管理します。
 * @author Xeltica
 */
object MetaModule : ModuleBase() {
    val currentVersion: String
        get() = XCorePlugin.instance.description.version
    var previousVersion: String? = null
        private set
    var isUpdated = false
        private set
    var postToDiscord = false
        private set

    // TODO: チェンジログをここではなく別ファイルに書いてそれを参照する。
    // やり方を調べる必要がある
    val changeLog = arrayOf(
        "メインワールドからのみイベントマップに行けるように変更",
        "参加時、通知があれば5秒後に表示するように"
    )

    override fun onEnable() {
        meta = Config("meta")
        checkUpdate()
    }

    override fun onPostEnable() {
        if (isUpdated) {
            var prev = previousVersion
            if (prev == null) prev = "unknown"
            val current = currentVersion
            val text = String.format("§aX-Coreを更新しました。%s -> %s", prev, current)
            if (postToDiscord) {
                DiscordModule.postChangelog(current, changeLog)
            }
            Bukkit.getServer()
                .audiences()
                .forEach {
                    it!!.sendMessage(Component.text(text))
                    for (log in changeLog) {
                        it.sendMessage(Component.text("・$log"))
                    }
                }
        }
    }

    private fun checkUpdate() {
        val conf = meta.conf
        val currentVersion = conf.getString("version", null)
        postToDiscord = conf.getBoolean("postToDiscord", false)
        if (!conf.contains("postToDiscord")) {
            conf["postToDiscord"] = false
        }
        previousVersion = conf.getString("previousVersion", null)
        if (currentVersion == null || currentVersion != currentVersion) {
            conf["version"] = currentVersion
            conf["previousVersion"] = currentVersion
            previousVersion = currentVersion
            isUpdated = true
            try {
                meta.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var meta: Config
}