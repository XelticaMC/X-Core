package work.xeltica.craft.core.modules.bedrock

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui

object BedrockModule : ModuleBase() {
    const val keyIsAcceptedDisclaimer = "accept_disclaimer"
    const val disclaimerTitle = "§l統合版プレイヤーのあなたへ"
    const val disclaimerMessage =
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

    fun showDisclaimerAsync(p: Player) {
        val fapi = FloodgateApi.getInstance()
        if (!fapi.isFloodgatePlayer(p.uniqueId)) return
        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable { showDisclaimer(p) }, 20)
    }

    fun showDisclaimer(p: Player) {
        Gui.getInstance().openDialog(p, disclaimerTitle, disclaimerMessage, {
            val record = PlayerStore.open(p)
            record[keyIsAcceptedDisclaimer] = true
        }, "わかりました")
    }
}