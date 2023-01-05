package work.xeltica.craft.core.modules.omikuji

import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import java.io.IOException
import java.util.Random

/**
 * おみくじ機能を提供するモジュールです。
 */
object OmikujiModule : ModuleBase() {
    lateinit var config: Config
    private val random = Random()

    override fun onEnable() {
        config = Config("omikujistore")
        registerHandler(OmikujiHandler())
        registerCommand("omikuji", OmikujiCommand())
    }

    /**
     * [player] のくじ引き結果の文字列表現を取得します。
     */
    fun getScoreName(player: Player): String {
        return getScoreName(get(player))
    }

    /**
     * [score] の文字列表現を取得します。
     */
    fun getScoreName(score: OmikujiScore): String {
        return score.displayName
    }

    /**
     * [player] が既におみくじを引いたかどうかを取得します。
     */
    fun isDrawnBy(player: Player): Boolean {
        return get(player) != OmikujiScore.NONE
    }

    /**
     * [player] のくじ引き結果を取得します。
     */
    fun get(player: Player): OmikujiScore {
        val str = config.conf.getString(player.uniqueId.toString()) ?: return OmikujiScore.NONE
        return OmikujiScore.getByDisplayName(str)
    }

    /**
     * [player] のくじ引き結果を [score] に設定します。
     */
    fun set(player: Player, score: OmikujiScore) {
        config.conf.set(player.uniqueId.toString(), score.displayName)
        saveConfig()
    }

    /**
     * くじ引き状況をリセットします。
     */
    fun reset() {
        config.conf.getKeys(false).forEach {
            config.conf.set(it, null)
        }
        saveConfig()
    }

    fun saveConfig() {
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * くじ引きを行い、結果の [OmikujiScore] を返します。
     */
    fun generateScore(): OmikujiScore {
        val dice = random.nextInt(100000)

        if (dice == 777) return OmikujiScore.TOKUDAIKICHI
        if (dice == 666) return OmikujiScore.DAIKYOU
        if (dice < 10000) return OmikujiScore.DAIKICHI
        if (dice < 30000) return OmikujiScore.KICHI
        if (dice < 55000) return OmikujiScore.CHUKICHI
        if (dice < 80000) return OmikujiScore.SHOKICHI
        return OmikujiScore.KYOU
    }
}