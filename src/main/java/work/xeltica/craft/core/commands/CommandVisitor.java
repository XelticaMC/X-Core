package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class CommandVisitor extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var manager = PlayerFlagsStore.getInstance();
        if (args.length == 0) {
            var isVisitor = manager.getVisitorMode(player);
            var isAutoVisitor = isVisitor && !manager.getVisitorMode(player, true);

            var message = isAutoVisitor ? "§aスタッフ不在のためオン" : isVisitor ? "§aオン" : "§cオフ";
            player.sendMessage("あなたの観光モードは、" + message + "§rです。");
            return true;
        }
        if (args.length != 1) return false;
        if (!player.hasPermission("xcore.command.visitor.set")) {
            player.sendMessage("§c権限がありません");
            return true;
        };
        var flag = args[0];
        if (flag.equals("on"))
            manager.setVisitorMode(player, true);
        else if (flag.equals("off"))
            manager.setVisitorMode(player, false);
        else
            return false;
        return true;
    }
    
}
