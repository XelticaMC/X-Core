package work.xeltica.craft.core.hooks

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi
import org.geysermc.floodgate.api.player.FloodgatePlayer
import work.xeltica.craft.core.api.HookBase

object FloodgateHook : HookBase() {
    lateinit var api: FloodgateApi private set

    override val isEnabled = Bukkit.getPluginManager().getPlugin("floodgate") != null

    override fun onEnable() {
        api = FloodgateApi.getInstance()
    }

    fun Player.isFloodgatePlayer(): Boolean {
        return api.isFloodgatePlayer(uniqueId)
    }

    fun Player.toFloodgatePlayer(): FloodgatePlayer? {
        return if (isFloodgatePlayer()) api.getPlayer(uniqueId) else null
    }
}