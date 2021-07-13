package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.stores.HubStore;

public class CommandHub extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var isAdmin = player.hasPermission("hub.admin") || player.isOp();
        var store = HubStore.getInstance();

        if (args.length == 0) {
            store.teleport(player, HubType.Main);
            return true;
        }

        if (!isAdmin) {
            player.sendMessage("/hub");
        }
        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage("/hub [help|classic]");
        } else if (args[0].equalsIgnoreCase("classic")) {
            store.teleport(player, HubType.Classic);
        } else {
            return false;
        }

        return true;
    }
}
