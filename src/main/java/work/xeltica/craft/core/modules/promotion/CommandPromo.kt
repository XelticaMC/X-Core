package work.xeltica.craft.core.modules.promotion

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

/**
 * 市民システムの情報表示コマンド
 * @author Lutica
 */
class CommandPromo : CommandPlayerOnlyBase() {
    override fun execute(sender: Player, command: Command, label: String, args: Array<out String>): Boolean {
        // 情報表示対象のプレイヤー。デフォルトではコマンド送信者自身を指す
        var player = sender
        if (args.isNotEmpty()) {
            val name = args[0]
            if (!sender.hasPermission("otanoshimi.command.promo.other")) {
                sender.sendMessage("§c権限がありません。")
                return true
            }
            val p = Bukkit.getPlayer(name)
            if (p == null) {
                sender.sendMessage("§cプレイヤーが見つかりませんでした。")
                return true
            }
            player = p
        }
        sender.sendMessage(PromotionModule.getPromoInfo(player))
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return null
    }
}