package work.xeltica.craft.core.modules

import kotlin.Throws
import java.io.IOException
import work.xeltica.craft.core.models.Ranking
import java.lang.IllegalArgumentException
import java.util.stream.Collectors
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import work.xeltica.craft.core.XCorePlugin
import com.gmail.filoghost.holographicdisplays.api.Hologram
import work.xeltica.craft.core.api.Config
import java.util.function.Consumer

/**
 * 時間計測カウンターの情報を管理します。
 */
object RankingModule : ModuleBase() {
    override fun onEnable() {
        rankingConfig = Config("ranking")
        renderAll()
    }

    /**
     * ランキングを新規作成します。
     * @throws IllegalArgumentException 既にランキングが存在する
     * @throws IOException 保存に失敗
     */
    @Throws(IOException::class)
    @JvmStatic
    fun create(name: String?, displayName: String?, isPlayerMode: Boolean): Ranking {
        require(!has(name))
        val ranking = Ranking(name, rankingConfig)
        ranking.setDisplayName(displayName, false)
        ranking.setIsPlayerMode(isPlayerMode, false)
        ranking.save()
        renderAll()
        return ranking
    }

    /**
     * ランキングを削除します。
     * @return ランキング削除に成功すればtrue、そうでなければfalse。
     * @throws IOException 保存に失敗
     */
    @Throws(IOException::class)
    @JvmStatic
    fun delete(name: String?): Boolean {
        if (!has(name)) return false
        rankingConfig.conf[name!!] = null
        rankingConfig.save()
        renderAll()
        return true
    }

    /**
     * ランキングが存在するかどうかを取得します。
     * @param name ランキング名
     * @return ランキングが存在すればtrue、しなければfalse
     */
    @JvmStatic
    fun has(name: String?): Boolean {
        return rankingConfig.conf.contains(name!!)
    }

    /**
     * 指定した名前のランキングを取得します。
     * @param name ランキング名
     * @return ランキング。存在しなければnull
     */
    @JvmStatic
    operator fun get(name: String?): Ranking? {
        return if (!has(name)) null else Ranking(name, rankingConfig)
    }

    /**
     * ランキングを全て取得します。
     * @return ランキング一覧。
     */
    @JvmStatic
    val all: Set<Ranking>
        get() = rankingConfig.conf.getKeys(false).stream()
            .map { name: String? -> Ranking(name, rankingConfig) }
            .collect(Collectors.toSet())

    @JvmStatic
    fun renderAll() {
        HologramsAPI.getHolograms(XCorePlugin.instance).forEach(Consumer { h: Hologram -> h.delete() })
        all.forEach(Consumer { ranks: Ranking ->
            val loc = ranks.hologramLocation ?: return@Consumer
            val isHidden = ranks.hologramHidden
            val holo = HologramsAPI.createHologram(XCorePlugin.instance, loc)
            holo.appendTextLine("§a§l" + ranks.displayName)
            val ranking = ranks.queryRanking()
            for (i in 0..9) {
                val name = if (isHidden) "??????????" else if (ranking.size <= i) "--------" else ranking[i].id
                val value = if (isHidden) "??????????" else if (ranking.size <= i) "------" else ranking[i].score
                val prefix = if (i == 0) "§e" else if (i == 1) "§f" else if (i == 2) "§6" else "§d"
                holo.appendTextLine(String.format("%s§l%d位: %s (%s)", prefix, i + 1, name, value))
            }
        })
    }

    /** ranking.yml  */
    private lateinit var rankingConfig: Config
}