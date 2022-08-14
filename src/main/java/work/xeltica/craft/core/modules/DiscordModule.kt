package work.xeltica.craft.core.modules

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

import java.util.stream.Stream

/**
 * @author raink1208
 */
object DiscordModule: ModuleBase() {
    /**
     * プレイヤーに紐づくDiscordユーザーを取得します。
     * @param player プレイヤー
     * @return 指定したプレイヤーに紐づく、Discordユーザー。未連携であれば null。
     */
    @JvmStatic
    fun getMember(player: Player): Member? {
        if (!linkedToDiscord(player)) return null

        val discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId())
        return DiscordSRV.getPlugin().getMainGuild().getMemberById(discordId)
    }

    /**
     * Discordに、処罰者を報告します。
     * @param badPlayerName 処罰対象者名
     * @param abuses 処罰理由
     * @param time 期限
     * @param command 処罰内容
     */
    @JvmStatic
    fun reportDiscord(badPlayerName: String, abuses: String, time: String, command: String) {
        val guild = DiscordSRV.getPlugin().mainGuild;
        val channel = guild.getGuildChannelById(reportChannelID);
        if (channel is TextChannel) {
            channel.sendMessage(String.format(reportTemplate, badPlayerName, abuses, time + command)).queue();
        }
    }

    /**
     * 新規さんの来訪を担当スタッフにメンションします。
     * @param newcomer 新規さん
     */
    @JvmStatic
    fun alertNewcomer(newcomer: Player) {
        val guild = DiscordSRV.getPlugin().getMainGuild()
        val channel = guild.getGuildChannelById(guideChannelId)
        if (channel is TextChannel) {
            val message = String.format("<@&%s> <@&%s> 新規さん「%s」がメインワールドに入りました。監視・案内をお願いします。", securityRoleId, guideRoleId, newcomer.getName())
            channel.sendMessage(message)
        }
    }

    /**
     * ゲーム内チャットにメッセージを送信します。
     * @param text 送信する内容
     */
    @JvmStatic
    fun broadcast(text: String) {
        val discord = DiscordSRV.getPlugin()
        discord.getOptionalTextChannel("global").sendMessage(text).queue()
    }

    /**
     * X-Core 変更内容を追記します。
     * @param version X-Core のバージョン
     * @param changeLog 変更内容
     */
    @JvmStatic
    fun postChangelog(version: String, changeLog: Array<String>) {
        val guild = DiscordSRV.getPlugin().mainGuild
        val channel = guild.getGuildChannelById(changelogChannelId)
        if (channel is TextChannel) {
            val builder = StringBuilder()
            builder.append("**X-Core ver${version}**\n")
            Stream.of(changeLog).forEach {
                builder.append('・').append(it).append('\n')
            }
            channel.sendMessage(builder.toString()).queue()
        }
    }

    private fun linkedToDiscord(player: Player): Boolean {
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: return false

        val ctx = luckPerms.contextManager.getContext(player)
        return ctx.contains("discordsrv:linked", "true")
    }

    private val reportTemplate = "%s が禁止行為「%s」のため%sされました"

    private val reportChannelID = "869939558165397596"
    private val lockRoleID = "873782147876552726"
    private val changelogChannelId = "872687386700689408"
    private val guideChannelId = "991964666051960872"

    private val guideRoleId = "991958217804492850"
    private val securityRoleId = "991957703930953758"
}
