package work.xeltica.craft.core.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;

/**
 * PvPの有効・無効を切り替えるコマンド
 * @author Xeltica
 */
public class CommandPvp extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        if (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) return false;

        final var world = player.getWorld();
        final var flag = args[0].equalsIgnoreCase("on");
        world.setPVP(flag);
        player.sendMessage(ChatColor.GOLD + String.format("ワールド %s のPvPを%sしました", world.getName(), flag ? "許可" : "禁止"));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        return COMPLETE_LIST_ONOFF;
    }

}
