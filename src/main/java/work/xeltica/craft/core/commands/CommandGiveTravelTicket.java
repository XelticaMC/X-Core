package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.travels.TicketType;
import work.xeltica.craft.core.utils.TravelTicketUtil;

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
            player.sendMessage(ChatColor.RED + "そのようなプレイヤーはいません");
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

}
