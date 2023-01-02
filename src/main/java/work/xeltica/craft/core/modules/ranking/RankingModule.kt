package work.xeltica.craft.core.modules.ranking

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import java.io.IOException

object RankingModule : ModuleBase() {
    private lateinit var config: Config

    override fun onEnable() {
        config = Config("ranking")
        registerCommand("ranking", RankingCommand())
    }

    @Throws(IOException::class)
    fun create(name: String, displayName: String, isPlayerMode: Boolean): Ranking {
        if (has(name)) throw IllegalArgumentException()

        val ranking = Ranking(name, config)
        ranking.setDisplayName(displayName, false)
        ranking.setIsPlayerMode(isPlayerMode, false)
        ranking.save()
        renderAll()
        return ranking
    }

    @Throws(IOException::class)
    fun delete(name: String): Boolean {
        if (!has(name)) return false

        config.conf.set(name, null)
        config.save()
        renderAll()
        return true
    }

    fun has(name: String): Boolean {
        return config.conf.contains(name)
    }

    operator fun get(name: String): Ranking? {
        if (!has(name)) return null
        return Ranking(name, config)
    }

    fun getAll(): Set<Ranking> {
        return config.conf.getKeys(false)
            .map { Ranking(it, config) }.toSet()
    }

    fun renderAll() {
        for (hologram in HologramsAPI.getHolograms(XCorePlugin.instance)) {
            hologram.delete()
        }
        for (ranks in getAll()) {
            val loc = ranks.getHologramLocation() ?: return
            val isHidden = ranks.getHologramHidden()
            val holo = HologramsAPI.createHologram(XCorePlugin.instance, loc)

            holo.appendTextLine("§a§l" + ranks.getDisplayName())
            val ranking = ranks.queryRanking()
            for (i in 0..10) {
                val name = if (isHidden) "??????????" else if (ranking.size <= i) "--------" else ranking[i].id
                val value = if (isHidden) "??????????" else if (ranking.size <= i) "--------" else ranking[i].score
                val prefix = if (i == 0) "§e" else if (i == 1) "§f" else if (i == 2) "§6" else "§d"
                holo.appendTextLine("%s§l%d位: %s (%s)".format(prefix, i + 1, name, value))
            }
        }
    }
}