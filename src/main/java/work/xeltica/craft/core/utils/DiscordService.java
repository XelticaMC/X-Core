package work.xeltica.craft.core.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

/**
 * @author raink1208
 */
public class DiscordService {
    public DiscordService() {
        instance = this;
    }

    public static DiscordService getInstance() {
        return instance;
    }

    /**
     * プレイヤーに紐づくDiscordユーザーを取得します。
     * @param player プレイヤー
     * @return 指定したプレイヤーに紐づく、Discordユーザー。未連携であれば null。
     */
    public Member getMember(Player player) {
        if (!linkedToDiscord(player)) return null;

        final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
        return DiscordSRV.getPlugin().getMainGuild().getMemberById(discordId);
    }

    /**
     * Discordに、処罰者を報告します。
     * @param badPlayerName 処罰対象者名
     * @param abuses 処罰理由
     * @param time 期限
     * @param command 処罰内容
     */
    public void reportDiscord(String badPlayerName, String abuses, String time, String command) {
        final var guild = DiscordSRV.getPlugin().getMainGuild();
        final var channel = guild.getGuildChannelById(reportChannelID);
        if (channel instanceof TextChannel textChannel) {
            textChannel.sendMessage(String.format(reportTemplate, badPlayerName, abuses, time + command)).queue();
        }
    }

    /**
     * 新規さんの来訪を担当スタッフにメンションします。
     * @param newcomer 新規さん
     */
    public void alertNewcomer(Player newcomer) {
        final var guild = DiscordSRV.getPlugin().getMainGuild();
        final var channel = guild.getGuildChannelById(guideChannelId);
        if (channel instanceof TextChannel textChannel) {
            final var message = String.format("<@&%s> <@&%s> 新規さん「%s」がメインワールドに入りました。監視・案内をお願いします。", securityRoleId, guideRoleId, newcomer.getName());
            textChannel.sendMessage(message);
        }
    }

    /**
     * ゲーム内チャットにメッセージを送信します。
     * @param text 送信する内容
     */
    public void broadcast(String text) {
        final var discord = DiscordSRV.getPlugin();
        discord.getOptionalTextChannel("global").sendMessage(text).queue();
    }

    /**
     * X-Core 変更内容を追記します。
     * @param version X-Core のバージョン
     * @param changeLog 変更内容
     */
    public void postChangelog(String version, String[] changeLog) {
        final var guild = DiscordSRV.getPlugin().getMainGuild();
        final var channel = guild.getGuildChannelById(changelogChannelId);
        if (channel instanceof TextChannel textChannel) {
            final var builder = new StringBuilder();
            builder.append("**X-Core** ").append(version).append('\n');
            Stream.of(changeLog).forEach(l -> builder.append('・').append(l).append('\n'));
            textChannel.sendMessage(builder.toString()).queue();
        }
    }

    private boolean linkedToDiscord(Player player) {
        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        final var luckPerms = provider.getProvider();

        final var ctx = luckPerms.getContextManager().getContext(player);
        return ctx.contains("discordsrv:linked", "true");
    }

    private static DiscordService instance;
    private final String reportTemplate = "%s が禁止行為「%s」のため%sされました";

    private final String reportChannelID = "869939558165397596";
    private final String lockRoleID = "873782147876552726";
    private final String changelogChannelId = "872687386700689408";
    private final String guideChannelId = "991964666051960872";

    private final String guideRoleId = "991958217804492850";
    private final String securityRoleId = "991957703930953758";
}
