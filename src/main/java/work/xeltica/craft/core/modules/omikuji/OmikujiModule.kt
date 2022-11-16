package work.xeltica.craft.core.modules.omikuji

import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.*

object OmikujiModule: ModuleBase() {
    lateinit var config: Config
    private val random = Random()

    override fun onEnable() {
        config = Config("omikujistore")
        registerHandler(OmikujiHandler())
        registerCommand("omikuji", OmikujiCommand())
    }

    fun getScoreName(player: Player): String {
        return getScoreName(get(player))
    }

    fun getScoreName(score: OmikujiScore): String {
        return score.displayName
    }

    fun isDrawnBy(player: Player): Boolean {
        return get(player) != OmikujiScore.NONE
    }

    fun get(player: Player): OmikujiScore {
        val str = config.conf.getString(player.uniqueId.toString()) ?: OmikujiScore.NONE.displayName
        return OmikujiScore.valueOf(str)
    }

    fun set(player: Player, score: OmikujiScore) {
        config.conf.set(player.uniqueId.toString(), score.displayName)
        saveConfig()
    }

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