package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandDepositClovers extends CommandBase {
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // CloverStore.getInstance().saveAllCloversAccount();
        sender.sendMessage("廃止。");
        return true;
    }
}
