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
        "システム内部を大規模に書き直しました。",
        "X Phone：/phone コマンドの挙動を、「メニューが開く」に変更しました。",
        "X Phone：/phone get コマンドでX Phoneアイテムを入手できるように変更しました。",
        "X Phone：/phone を /p と書けるようにしました。",
        "看板編集：スニーク状態で看板を右クリックすると、編集できる機能を追加しました。",
        "モブボール：アイアンゴーレムおよびスノーゴーレムを捕獲時に、それぞれのスポーンエッグ柄のモブケースになるように変更しました。",
        "モブボール：モブケースからモブを出すときの挙動を、バニラのアイテムに近づくよう改善しました。",
        "モブボール：モブボールで捕獲したモブはデスポーンしないよう変更しました。",
        "エビパワー没収の対象になるモブでできたNPCを殴ると、ダメージが入らないのにエビパワーだけ没収される問題を修正しました。",
        "自動クラフト：カスタムアイテムをクラフトに使用できてしまう問題を修正しました。",
        "ネコを殴ったときの効果音が正しくない問題を修正しました。",
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