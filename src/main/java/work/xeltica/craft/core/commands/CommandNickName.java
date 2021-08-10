package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.stores.NickNameStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author raink1208
 */
public class CommandNickName extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        NickNameStore.getInstance().setNickNameType(player.getUniqueId(), args[0]);
        player.sendMessage("NickNameTypeを" + args[0] + "に変更しました");
        NickNameStore.getInstance().setNickName(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return COMMANDS;
    }

    private static final List<String> COMMANDS = new ArrayList<>(Arrays.asList("minecraft", "discord", "discord-nick"));
}
