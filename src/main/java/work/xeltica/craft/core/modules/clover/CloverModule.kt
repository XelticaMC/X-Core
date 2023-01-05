package work.xeltica.craft.core.modules.clover

import com.google.common.collect.Lists
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.VaultHook
import java.io.IOException

object CloverModule : ModuleBase() {
    lateinit var clovers: Config

    override fun onEnable() {
        clovers = Config("clovers")
        registerHandler(CloverHandler())
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