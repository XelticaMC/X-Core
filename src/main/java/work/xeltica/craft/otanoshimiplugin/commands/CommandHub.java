package work.xeltica.craft.otanoshimiplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.otanoshimiplugin.stores.HubStore;

public class CommandHub extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var isAdmin = player.hasPermission("hub.admin") || player.isOp();
        var store = HubStore.getInstance();

        if (args.length == 0) {
            store.teleport(player);
            return true;
        }

        if (!isAdmin) {
            player.sendMessage("/hub");
        }
        if (args[0].equalsIgnoreCase("create")) {
            store.CreateHub();
            player.sendMessage("Generated!");
        } else if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage("/hub [create/help/main/delete/unload/update/reloadplayers/forceall]");
        } else if (args[0].equalsIgnoreCase("unload")) {
            if (!store.tryUnload()) {
                player.sendMessage("hub が未生成");
            }
        } else if (args[0].equalsIgnoreCase("update")) {
            if (!store.tryUpdate()) {
                player.sendMessage("hub が未生成");
            }
        } else if (args[0].equalsIgnoreCase("reloadplayers")) {
            store.reloadPlayers();
        } else if (args[0].equalsIgnoreCase("forceall")) {
            store.setForceAll(!store.getForceAll());
            player.sendMessage("forceAll を" + store.getForceAll() + "にしました");
        } else {
            return false;
        }

        return true;
    }
}
