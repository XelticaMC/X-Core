package work.xeltica.craft.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

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

}
