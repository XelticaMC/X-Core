package work.xeltica.craft.core.api.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.api.commands.CommandBase;

/**
 * プレイヤー限定のコマンドの基底クラス
 * @author Xeltica
 */
public abstract class CommandPlayerOnlyBase extends CommandBase {
    /**
     * プレイヤーがコマンドが呼び出されたときに動く関数
     * @param player コマンドを実行したプレイヤーの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label 親コマンドが入っている
     * @param args 子コマンド(引数)が入っている
     * @return 正常に実行された場合はtrue、そうでない場合はfalse
     */
    public abstract boolean execute(Player player, Command command, String label, String[] args);

    @Override
    public final boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが実行してください");
            return true;
        }
        return execute((Player)sender, command, label, args);
    }
}
