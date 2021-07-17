package work.xeltica.craft.otanoshimiplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import work.xeltica.craft.otanoshimiplugin.stores.CloverStore;

public class CommandDepositClovers extends CommandBase {
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        CloverStore.getInstance().saveAllCloversAccount();
        return true;
    }
}
