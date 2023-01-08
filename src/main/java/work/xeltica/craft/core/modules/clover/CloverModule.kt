package work.xeltica.craft.core.modules.clover

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import java.io.IOException

/**
 * XelticaMC の旧通貨「クローバー」のバックアップを管理するモジュールです。
 */
object CloverModule : ModuleBase() {
    lateinit var clovers: Config

    override fun onEnable() {
        clovers = Config("clovers")
        registerHandler(CloverHandler())
    }

    /**
     * [player] の所有クローバーを取得します。
     */
    fun getCloverOf(player: OfflinePlayer): Double {
        return clovers.conf.getDouble(player.uniqueId.toString())
    }

    /**
     * [player] の所有クローバーを [balance] に設定します。
     */
    operator fun set(player: Player, balance: Double) {
        clovers.conf[player.uniqueId.toString()] = balance
    }

    /**
     * [player] の所有クローバーを消去します。
     */
    fun delete(player: Player) {
        clovers.conf[player.uniqueId.toString()] = null
    }

    /**
     * クローバーのバックアップを保存します。
     */
    @Throws(IOException::class)
    fun save() {
        clovers.save()
    }
}