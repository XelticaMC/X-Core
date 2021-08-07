package work.xeltica.craft.core.stores;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.utils.Config;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author raink1208
 */
public class NickNameStore {
    public NickNameStore() {
        instance = this;
        config = new Config("nickname");
    }

    public static NickNameStore getInstance() { return instance; }

    public String getNickName(UUID uuid, String type) {
        return switch (type) {
            case "discord" -> getDiscordMember(uuid).getUser().getName();
            case "discord-nick" -> getDiscordMember(uuid).getNickname();
            default -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName();
        };
    }

    public void setNickName(Player player) {
        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        final var luckPerms = provider.getProvider();
        final var ctx = luckPerms.getContextManager().getContext(player);
        String type = getNickNameType(player.getUniqueId());

        if (!ctx.contains("discordsrv:linked", "true")) {
            type = "minecraft";
        }

        final String nickname = getNickName(player.getUniqueId(), type);

        if (nicknameLength(nickname) > nicknameLimit) {
            player.sendMessage("nicknameの長さが " + nicknameLimit + "文字 より長いので変更できませんでした");
            return;
        }

        player.setCustomName(nickname);
        player.setPlayerListName(nickname);
        player.setDisplayName(nickname);
    }

    public String getNickNameType(UUID uuid) {
        final var type = config.getConf().get(uuid.toString());
        if (type instanceof String) {
            return (String) type;
        }
        return "null";
    }

    public void setNickNameType(UUID uuid, String type) {
        config.getConf().set(uuid.toString(), type);
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Member getDiscordMember(UUID uuid) {
        final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid);
        return DiscordSRV.getPlugin().getMainGuild().getMemberById(discordId);
    }

    private Integer nicknameLength(String nickname) {
        double length = 0.0;
        for (Character c: nickname.toCharArray()) {
            if (String.valueOf(c).getBytes().length < 2) {
                length += 0.5;
            } else {
                length += 1;
            }
        }
        return (int) Math.ceil(length);
    }

    private static NickNameStore instance;
    private final Config config;

    private final Integer nicknameLimit = 8;
}