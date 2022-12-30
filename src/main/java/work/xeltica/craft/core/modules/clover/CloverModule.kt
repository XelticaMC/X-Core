package work.xeltica.craft.core.modules.clover

import com.google.common.collect.Lists
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.VaultHook
import work.xeltica.craft.core.utils.Config
import java.io.IOException

object CloverModule: ModuleBase() {
    lateinit var clovers: Config

    override fun onEnable() {
        clovers = Config("clovers")
    }

    fun saveAllCloversAccount() {
        val players = Lists.newArrayList(*Bukkit.getOfflinePlayers())
        val logger = Bukkit.getLogger()
        players.addAll(Bukkit.getOnlinePlayers())
        for (p in players) {
            val balance = VaultHook.getBalance(p)
            if (balance == 0.0) continue
            clovers.conf.set(p.uniqueId.toString(), balance)
            logger.info(String.format("%sさんの残高 %f Clover をデポジット", p.name, balance))
        }
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
}