package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.stores.WorldStore;

/**
 * 指定したワールドの最後にいた場所に転送するコマンド
 * @author Xeltica
 */
public class CommandXtp extends CommandBase {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
        if (args.length == 2 && !sender.hasPermission("otanoshimi.command.xtp.other")) {
            sender.sendMessage("§c権限がありません。");
            return true;
        }
        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage("プレイヤーが実行してください。");
            return true;
        }
        var worldName = args[0];
        var p = args.length == 2 ? Bukkit.getPlayer(args[1]) : (Player)sender;
        if (p == null) {
            sender.sendMessage("§cプレイヤーが存在しません");
            return true;
        }
        WorldStore.getInstance().teleportToSavedLocation(p, worldName);
        return true;
    }
    
}
