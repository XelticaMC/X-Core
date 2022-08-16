package work.xeltica.craft.core.modules

import org.bukkit.entity.Player
import java.io.IOException
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config

/**
 * @author raink1208
 */
object QuickChatModule : ModuleBase() {
    @JvmStatic
    val allPrefix: Set<String> get() = config.conf.getKeys(false)

    override fun onEnable() {
        XCorePlugin.instance.saveResource("quickChats.yml", false)
        config = Config("quickChats")
    }

    @JvmStatic
    fun getMessage(prefix: String?): String {
        return config.conf.getString(prefix!!)!!
    }

    @JvmStatic
    fun chatFormat(msg: String, player: Player): String {
        var message = msg
        message = message.replace("{world}", WorldManagementModule.getWorldDisplayName(player.world) ?: "不明")
        message = message.replace("{x}", player.location.blockX.toString())
        message = message.replace("{y}", player.location.blockY.toString())
        message = message.replace("{z}", player.location.blockZ.toString())
        return message
    }

    @JvmStatic
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

    @JvmStatic
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

    private lateinit var config: Config
}