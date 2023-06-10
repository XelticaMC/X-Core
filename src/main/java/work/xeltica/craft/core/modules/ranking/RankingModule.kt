package work.xeltica.craft.core.modules.ranking

import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import java.io.IOException

/**
 * ランキング機能を提供するモジュールです。
 */
object RankingModule : ModuleBase() {
    private lateinit var config: Config

    override fun onEnable() {
        config = Config("ranking")
        registerCommand("ranking", RankingCommand())
    }

    /**
     * ランキングボード [name] を作成します。
     * [isPlayerMode] を true にすると、キーにUUIDを受け付けるようになり、表示時にプレイヤーの名前として表示されます。
     */
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

    /**
     * ランキングボード [name] を削除します。
     */
    @Throws(IOException::class)
    fun delete(name: String): Boolean {
        if (!has(name)) return false

        config.conf.set(name, null)
        config.save()
        renderAll()
        return true
    }

    /**
     * ランキングボード [name] が存在するかどうかを取得します。
     */
    fun has(name: String): Boolean {
        return config.conf.contains(name)
    }

    /**
     * ランキングボード [name] を取得します。
     */
    operator fun get(name: String): Ranking? {
        if (!has(name)) return null
        return Ranking(name, config)
    }

    /**
     * ランキングボードを全て取得します。
     */
    fun getAll(): Set<Ranking> {
        return config.conf.getKeys(false)
            .map { Ranking(it, config) }.toSet()
    }

    /**
     * ワールドに生成されたランキングボードを再描画します。
     */
    fun renderAll() {
    }
}