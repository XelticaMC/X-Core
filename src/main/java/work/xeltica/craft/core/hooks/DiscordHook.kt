package work.xeltica.craft.core.hooks

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.HookBase

/**
 * Discord との連携フック。
 */
object DiscordHook : HookBase() {
    override val isEnabled = Bukkit.getPluginManager().getPlugin("DiscordSRV")?.isEnabled ?: false

    /**
     * プレイヤーに紐づくDiscordユーザーを取得します。
     * @param player プレイヤー
     * @return 指定したプレイヤーに紐づく、Discordユーザー。未連携であれば null。
     */
    fun getMember(player: Player): Member? {
        if (!isEnabled) return null
        if (!linkedToDiscord(player)) return null
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(player.uniqueId)
        return DiscordSRV.getPlugin().mainGuild.getMemberById(discordId)
    }

    /**
     * Discordに、処罰者を報告します。
     * @param badPlayerName 処罰対象者名
     * @param abuses 処罰理由
     * @param time 期限
     * @param command 処罰内容
     */
    fun reportDiscord(badPlayerName: String, abuses: String, time: String, command: String) {
        if (!isEnabled) return
        val guild = DiscordSRV.getPlugin().mainGuild
        val channel: GuildChannel = guild.getGuildChannelById(reportChannelID) ?: return
        if (channel !is TextChannel) return
        channel.sendMessage(String.format(reportTemplate, badPlayerName, abuses, time + command)).queue()
    }

    /**
     * 新規さんの来訪を担当スタッフにメンションします。
     * @param newcomer 新規さん
     */
    fun alertNewcomer(newcomer: Player) {
        if (!isEnabled) return
        val guild = DiscordSRV.getPlugin().mainGuild
        val channel = guild.getGuildChannelById(guideChannelId)
        if (channel is TextChannel) {
            val message = String.format(
                "<@&%s> <@&%s> 新規さん「%s」がメインワールドに入りました。監視・案内をお願いします。",
                securityRoleId,
                guideRoleId,
                newcomer.name
            )
            channel.sendMessage(message)
        }
    }

    /**
     * ゲーム内チャットにメッセージを送信します。
     * @param text 送信する内容
     */
    fun broadcast(text: String) {
        if (!isEnabled) return
        val discord = DiscordSRV.getPlugin()
        discord.getOptionalTextChannel("global").sendMessage(text).queue()
    }

    /**
     * X-Core 変更内容を追記します。
     * @param version X-Core のバージョン
     * @param changeLog 変更内容
     */
    fun postChangelog(version: String, changeLog: Array<String>) {
        if (!isEnabled) return
        val guild = DiscordSRV.getPlugin().mainGuild
        val channel = guild.getGuildChannelById(changelogChannelId)
        if (channel is TextChannel) {
            val builder = StringBuilder()
            builder.append("**X-Core** ").append(version).append('\n')
            changeLog.forEach {
                builder.append('・').append(it).appendLine()
            }
            channel.sendMessage(builder.toString()).queue()
        }
    }

    private fun linkedToDiscord(player: Player): Boolean {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        val luckPerms = provider!!.provider
        val ctx = luckPerms.contextManager.getContext(player)
        return ctx.contains("discordsrv:linked", "true")
    }

    private const val reportTemplate = "%s が禁止行為「%s」のため%sされました"

    private const val reportChannelID = "869939558165397596"
    private const val lockRoleID = "873782147876552726"
    private const val changelogChannelId = "872687386700689408"
    private const val guideChannelId = "991964666051960872"

    private const val guideRoleId = "991958217804492850"
    private const val securityRoleId = "991957703930953758"
}