package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * X-Core コマンドの基底クラス。
 * @author Xeltica
 */
public abstract class CommandBase {
    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
}
