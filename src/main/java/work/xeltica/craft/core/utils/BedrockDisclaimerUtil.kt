package work.xeltica.craft.core.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.player.PlayerModule.open

/**
 * 統合版プレイヤー向けに免責事項を表示するためのメソッドを持ちます。
 * @author Xeltica
 */
object BedrockDisclaimerUtil {
    const val BEDROCK_DISCLAIMER_TITLE = "§l統合版プレイヤーのあなたへ"
    const val BEDROCK_DISCLAIMER_MESSAGE =
        "本サーバーはJava版と統合版の両方に対応しておりますが、サーバーはJava版となっております。 Java版と統合版は細部の仕様が異なり、それに起因する不具合や差異があります。例えば、\n" +
                "\n" +
                "・看板の文字数が合わない\n" +
                "・竹ブロックの当たり判定がおかしい\n" +
                "・スキンの互換性が無い\n" +
                "・赤石の挙動が一部異なる\n" +
                "\n" +
                "などです。統合版への対応はまだ不安定でバグも多いこと、突然統合版への対応を打ち切るかもしれないということをご理解ください。\n" +
                "なお、問題が発生しましたらDiscordにてサポートを受け付けています。詳しくは公式サイトをご確認ください。"

    fun showDisclaimerAsync(p: Player) {
        val fapi = FloodgateApi.getInstance()
        if (!fapi.isFloodgatePlayer(p.uniqueId)) return
        Bukkit.getScheduler().runTaskLater(instance, Runnable { showDisclaimer(p) }, 20)
    }

    fun showDisclaimer(p: Player) {
        getInstance().openDialog(p, BEDROCK_DISCLAIMER_TITLE, BEDROCK_DISCLAIMER_MESSAGE, {
            val record = open(p)
            record[PlayerDataKey.BEDROCK_ACCEPT_DISCLAIMER] = true
        }, "わかりました")
    }
}