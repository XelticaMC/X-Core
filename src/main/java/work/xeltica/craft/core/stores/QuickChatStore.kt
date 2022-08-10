package work.xeltica.craft.core.stores

import org.bukkit.entity.Player
import java.io.IOException
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config

/**
 * @author raink1208
 */
class QuickChatStore {
    val allPrefix: Set<String>
        get() = config.conf.getKeys(false)

    fun getMessage(prefix: String?): String {
        return config.conf.getString(prefix!!)!!
    }

    fun chatFormat(msg: String, player: Player): String {
        var msg = msg
        msg = msg.replace("{world}", WorldStore.getInstance().getWorldDisplayName(player.world))
        msg = msg.replace("{x}", player.location.blockX.toString())
        msg = msg.replace("{y}", player.location.blockY.toString())
        msg = msg.replace("{z}", player.location.blockZ.toString())
        return msg
    }

    fun register(prefix: String?, msg: String?): Boolean {
        if (config.conf.contains(prefix!!)) return false
        config.conf[prefix] = msg
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    fun unregister(prefix: String?): Boolean {
        if (config.conf.contains(prefix!!)) {
            config.conf[prefix] = null
            try {
                config.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return true
        }
        return false
    }

    private val config: Config

    init {
        instance = this
        XCorePlugin.instance.saveResource("quickChats.yml", false)
        config = Config("quickChats")
    }

    companion object {
        @JvmStatic
        lateinit var instance: QuickChatStore
            private set
    }
}