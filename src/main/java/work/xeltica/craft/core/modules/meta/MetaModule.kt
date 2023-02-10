package work.xeltica.craft.core.modules.meta

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.DiscordHook
import java.io.IOException

/**
 * X-Core のバージョンおよびチェンジログを保持し、更新を検知して通知する機能を提供するモジュールです。
 */
object MetaModule : ModuleBase() {
    val changeLog = listOf(
        "新機能を追加しました。近日中に別途アナウンスします。",
        "看板をスニーク状態で右クリックすると編集できる機能において、アイテムを何も持っていないときのみ動作するよう変更しました。",
        "カボチャおよびスイカを破壊したとき、ヒント「エビパワーマイニング」を達成するようになりました。",
        "カボチャおよびスイカを破壊したとき、1つあたり1EP入手できるようになりました。",
        "あらゆるブロックの破壊が「作物の収穫」としてエビパワー入手対象となっていた不具合を修正しました。",
    ).toTypedArray()

    lateinit var meta: Config
    var currentVersion: String? = null; private set
    var previousVersion: String? = null; private set
    var isUpdated = false; private set
    var postToDiscord = false; private set

    override fun onEnable() {
        meta = Config("meta")
        currentVersion = XCorePlugin.instance.description.version
        checkUpdate()

        if (isUpdated) {
            val prev = previousVersion ?: "unknown"
            val current = currentVersion!!
            val text = "${ChatColor.GREEN}X-Coreを${prev}から${current}へ更新しました。"
            if (postToDiscord) {
                DiscordHook.postChangelog(current, changeLog)
            }
            with(Bukkit.getServer()) {
                sendMessage(Component.text(text))
                changeLog.forEach {
                    sendMessage(Component.text("・$it"))
                }
            }
        }
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