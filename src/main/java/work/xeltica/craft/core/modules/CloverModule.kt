package work.xeltica.craft.core.modules

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import kotlin.Throws
import java.io.IOException

/**
 * 廃止前のクローバー情報を格納するストアです。
 * 以前は全プレイヤーの所有クローバーをアーカイブする機能を備えていましたが、
 * クローバー廃止に伴い廃止しました。クローバーをエビパワーに変換し次第
 * このクラスと関連クラスは廃止します。
 * @author Xeltica
 */
object CloverModule : ModuleBase() {
    override fun onEnable() {
        clovers = Config("clovers")
    }

    fun getCloverOf(p: OfflinePlayer): Double {
        return clovers.conf.getDouble(p.uniqueId.toString())
    }

    operator fun set(player: Player, balance: Double) {
        clovers.conf[player.uniqueId.toString()] = balance
    }

    fun delete(player: Player) {
        clovers.conf[player.uniqueId.toString()] = null
    }

    @Throws(IOException::class)
    fun save() {
        clovers.save()
    }

    private lateinit var clovers: Config
}