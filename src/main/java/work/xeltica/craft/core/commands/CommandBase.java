package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * X-Core コマンドの基底クラス。
 * @author Xeltica
 */
public abstract class CommandBase {
    /**
     * コマンドが呼び出されたときに動く関数
     * @param sender コマンドを実行したプレイヤーの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label 親コマンドが入っている
     * @param args 子コマンド(引数)が入っている
     * @return 正常に実行された場合はtrue、そうでない場合はfalse
     */
    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
}
