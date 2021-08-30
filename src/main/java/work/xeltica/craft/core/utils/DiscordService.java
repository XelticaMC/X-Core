package work.xeltica.craft.core.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public Member getMember(Player player) {
        if (!linkedToDiscord(player)) return null;

        final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
        return DiscordSRV.getPlugin().getMainGuild().getMemberById(discordId);
    }

    public void reportDiscord(Player badPlayer, String abuses, String time, String command) {
        final var guild = DiscordSRV.getPlugin().getMainGuild();
        final var channel = guild.getGuildChannelById(reportChannelID);
        if (channel instanceof TextChannel textChannel) {
            textChannel.sendMessage(String.format(reportTemplate, badPlayer.getName(), abuses, time+command)).queue();
            final var member = getMember(badPlayer);
            if (member != null) {
                final var role = guild.getRoleById(lockRoleID);
                if (role != null) guild.addRoleToMember(member, role).queue();
            }
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
}
