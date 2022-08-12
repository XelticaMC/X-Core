package work.xeltica.craft.core.modules

import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.models.OmikujiScore
import java.io.IOException
import java.util.*
import java.util.function.Consumer

/**
 * プレイヤーのおみくじ記録を保存・読み出しします。
 * @author Xeltica
 */
object OmikujiModule : ModuleBase() {
    override fun onEnable() {
        cm = Config("omikujistore")
    }

    @Deprecated("")
    @JvmStatic
    fun getScoreName(score: OmikujiScore): String {
        return score.displayName
    }

    @JvmStatic
    fun getScoreName(player: Player): String {
        return getScoreName(get(player))
    }

    @JvmStatic
    fun isDrawnBy(player: Player): Boolean {
        return get(player) !== OmikujiScore.None
    }

    @JvmStatic
    operator fun get(player: Player): OmikujiScore {
        val str = cm.conf.getString(player.uniqueId.toString(), OmikujiScore.None.name)!!
        return OmikujiScore.valueOf(str)
    }

    @JvmStatic
    operator fun set(player: Player, score: OmikujiScore) {
        cm.conf[player.uniqueId.toString()] = score.name
        try {
            cm.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun reset() {
        cm.conf.getKeys(false).forEach(Consumer { key: String? -> cm.conf[key!!] = null })
        try {
            cm.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun generateScore(): OmikujiScore {
        val dice = random.nextInt(100000)
        if (dice == 777) return OmikujiScore.Tokudaikichi
        if (dice == 666) return OmikujiScore.Daikyou
        if (dice < 10000) return OmikujiScore.Daikichi
        if (dice < 30000) return OmikujiScore.Kichi
        if (dice < 55000) return OmikujiScore.Chukichi
        return if (dice < 80000) OmikujiScore.Shokichi else OmikujiScore.Kyou
    }

    private lateinit var cm: Config
    private val random = Random()
}