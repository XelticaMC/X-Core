package work.xeltica.craft.core.api.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * プレイヤー限定のコマンドの基底クラス
 * @author Xeltica
 */
abstract class CommandPlayerOnlyBase : CommandBase() {
    /**
     * プレイヤーがコマンドが呼び出されたときに動く関数
     * @param player コマンドを実行したプレイヤーの情報が入っている
     * @param command 実行されたコマンドに関する情報が入っている
     * @param label 親コマンドが入っている
     * @param args 子コマンド(引数)が入っている
     * @return 正常に実行された場合はtrue、そうでない場合はfalse
     */
    abstract fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}プレイヤーが実行してください")
            return true
        }
        return execute(sender, command, label, args)
    }
}