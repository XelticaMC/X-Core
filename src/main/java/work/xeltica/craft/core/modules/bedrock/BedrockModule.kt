package work.xeltica.craft.core.modules.bedrock

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.FloodgateHook.isFloodgatePlayer

/**
 * 統合版プレイヤーに対する追加サポートを提供するモジュールです。
 */
object BedrockModule : ModuleBase() {
    const val PS_KEY_ACCEPT_DISCLAIMER = "accept_disclaimer"

    /**
     * 免責事項タイトル
     */
    private val DISCLAIMER_TITLE = "${ChatColor.BOLD}統合版プレイヤーのあなたへ"

    /**
     * 免責事項のメッセージ本文
     */
    private const val DISCLAIMER_MESSAGE =
        """本サーバーはJava版と統合版の両方に対応しておりますが、サーバーはJava版となっております。 Java版と統合版は細部の仕様が異なり、それに起因する不具合や差異があります。例えば、

・看板の文字数が合わない
・竹ブロックの当たり判定がおかしい
・スキンの互換性が無い
・赤石の挙動が一部異なる

などです。統合版への対応はまだ不安定でバグも多いこと、突然統合版への対応を打ち切るかもしれないということをご理解ください。
なお、問題が発生しましたらDiscordにてサポートを受け付けています。詳しくは公式サイトをご確認ください。"""

    override fun onEnable() {
        registerHandler(BedrockHandler())
    }

    /**
     * 1秒後に [player] に免責事項を表示します。
     */
    fun showDisclaimerAsync(player: Player) {
        if (!player.isFloodgatePlayer()) return
        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable { showDisclaimer(player) }, 20)
    }

    /**
     * [player] に免責事項を表示します。
     */
    fun showDisclaimer(player: Player) {
        Gui.getInstance().openDialog(player, DISCLAIMER_TITLE, DISCLAIMER_MESSAGE, {
            val record = PlayerStore.open(player)
            record[PS_KEY_ACCEPT_DISCLAIMER] = true
        }, "わかりました")
    }
}