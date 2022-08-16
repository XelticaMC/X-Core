package work.xeltica.craft.core.modules

import net.luckperms.api.LuckPerms
import java.io.IOException
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import java.util.*
import kotlin.math.ceil

/**
 * @author raink1208
 */
object NickNameModule : ModuleBase() {
    override fun onEnable() {
        config = Config("nickname")
    }

    @JvmStatic
    fun getNickName(uuid: UUID, type: String): String {
        val mcName = (Bukkit.getPlayer(uuid) ?: throw IllegalArgumentException()).name
        return when (type) {
            "discord" -> getDiscordMember(uuid)?.user?.name ?: mcName
            "discord-nick" -> getDiscordMember(uuid)?.nickname ?: mcName
            else -> mcName
        }
    }

    @JvmStatic
    fun setNickName(player: Player) {
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        val luckPerms = provider!!.provider
        val ctx = luckPerms.contextManager.getContext(player)
        var type = getNickNameType(player.uniqueId)
        if (!ctx.contains("discordsrv:linked", "true")) {
            type = "minecraft"
        }
        val nickname = getNickName(player.uniqueId, type)
        if (nicknameLength(nickname) > nicknameLimit) {
            player.sendMessage("nicknameの長さが " + nicknameLimit + "文字 より長いので変更できませんでした")
            return
        }
        player.customName = nickname
        player.setPlayerListName(nickname)
        player.setDisplayName(nickname)
    }

    @JvmStatic
    fun getNickNameType(uuid: UUID): String {
        val type = config.conf[uuid.toString()]
        return if (type is String) type else "null"
    }

    @JvmStatic
    fun setNickNameType(uuid: UUID, type: String?) {
        config.conf[uuid.toString()] = type
        try {
            config.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getDiscordMember(uuid: UUID?): Member? {
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        return DiscordSRV.getPlugin().mainGuild.getMemberById(discordId)
    }

    private fun nicknameLength(nickname: String): Int {
        var length = 0.0
        for (c in nickname.toCharArray()) {
            length += if (c.toString().toByteArray().size < 2) 0.5 else 1.0
        }
        return ceil(length).toInt()
    }

    private lateinit var config: Config
    private const val nicknameLimit = 8
}