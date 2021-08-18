package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.travels.TicketType;
import work.xeltica.craft.core.utils.TravelTicketUtil;

import java.util.List;
import java.util.Objects;

/**
 * 旅行券をgiveするコマンド
 * @author Xeltica
 */
public class CommandGiveTravelTicket extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var name = args.length >= 1 ? args[0] : null;
        final var p = name == null ? player : Bukkit.getPlayer(name);
        if (p == null) {
            Objects.requireNonNull(player).sendMessage(ChatColor.RED + "そのようなプレイヤーはいません");
            return true;
        }
        try {
        final var typeString = args.length >= 2 ? args[1] : null;
        final var type = typeString == null ? TicketType.WILDAREA : TicketType.valueOf(typeString);
        final var amount = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
        p.getInventory().addItem(TravelTicketUtil.GenerateTravelTicket(amount, type));
        } catch (IllegalArgumentException e) {
            player.sendMessage("引数がおかしい");
            return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return COMPLETE_LIST_EMPTY;
    }
}
