package work.xeltica.craft.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * X-Core コマンドの基底クラス。
 * @author Xeltica
 */
abstract class CommandBase : TabCompleter {
    /**
     * コマンドが呼び出されたときに動く関数
     * @param sender コマンドを実行したプレイヤーかコンソールの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label コマンド名が入っている
     * @param args 引数
     * @return 正常に実行された場合はtrue、そうでない場合はfalse
     */
    abstract fun execute(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean

    /**
     * プレイヤーがコマンドを入力している途中で表示する入力候補を決定する関数
     * @param commandSender コマンドを入力中のプレイヤーかコンソールの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label コマンド名が入っている
     * @param args 引数
     * @return 表示する入力候補のリスト。デフォルト値を表示する場合はnull。
     */
    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        return null
    }
}