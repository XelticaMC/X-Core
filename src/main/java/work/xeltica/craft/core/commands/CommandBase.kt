package work.xeltica.craft.core.commands;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

/**
 * X-Core コマンドの基底クラス。
 * @author Xeltica
 */
public abstract class CommandBase implements TabCompleter {
    /**
     * コマンドが呼び出されたときに動く関数
     * @param sender コマンドを実行したプレイヤーかコンソールの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label コマンド名が入っている
     * @param args 引数
     * @return 正常に実行された場合はtrue、そうでない場合はfalse
     */
    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    /**
     * プレイヤーがコマンドを入力している途中で表示する入力候補を決定する関数
     * @param sender コマンドを入力中のプレイヤーかコンソールの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label コマンド名が入っている
     * @param args 引数
     * @return 表示する入力候補のリスト。デフォルト値を表示する場合はnull。
     */
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label, String[] args) {
        return null;
    }

    protected static final List<String> COMPLETE_LIST_EMPTY = new ArrayList<>();
    protected static final List<String> COMPLETE_LIST_ONOFF = List.of("on", "off");
}
