package work.xeltica.craft.core.modules.quickchat

import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.stores.WorldStore
import work.xeltica.craft.core.utils.Config
import java.io.IOException

/**
 * @author raink1208
 */
object QuickChatModule: ModuleBase() {
    lateinit var config: Config

    override fun onEnable() {
        config = Config("quickChats")
        registerCommand("qchat", QuickChatCommand())
        registerHandler(QuickChatHandler())
    }

    fun getAllPrefix(): Set<String> {
        return config.conf.getKeys(false)
    }

    fun getMessage(prefix: String): String {
        return config.conf.getString(prefix) ?: prefix
    }

    fun chatFormat(msg: String, player: Player): String {
        var text = msg

        text = text.replace("{world}", WorldStore.getInstance().getWorldDisplayName(player.world))
        text = text.replace("{x}", player.location.blockX.toString())
        text = text.replace("{y}", player.location.blockY.toString())
        text = text.replace("{z}", player.location.blockZ.toString())

        return text
    }

    fun register(prefix: String, msg: String): Boolean {
        if (config.conf.contains(prefix)) return false

        config.conf.set(prefix, msg)
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    fun unregister(prefix: String): Boolean {
        if (!config.conf.contains(prefix)) return false
        config.conf.set(prefix, null)
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }
}