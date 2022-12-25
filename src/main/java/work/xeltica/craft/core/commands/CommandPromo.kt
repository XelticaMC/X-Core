package work.xeltica.craft.core.commands

import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import net.luckperms.api.query.QueryOptions
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.player.PlayerModule
import work.xeltica.craft.core.utils.Ticks

/**
 * 市民システムの情報表示コマンド
 * @author Xeltica
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
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        val luckPerms = provider!!.provider
        val lpUser = luckPerms.getPlayerAdapter(Player::class.java).getUser(player)
        val playerModule = PlayerModule
        val record = playerModule.open(player)
        val isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream()
            .anyMatch { g: Group -> g.name == "citizen" }
        if (!playerModule.isCitizen(player)) {
            sender.sendMessage("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。")
            sender.sendMessage("§b市民§rにならなくても基本的なプレイはできますが、")
            sender.sendMessage("・§c一部ブロックが使えない§r")
            sender.sendMessage("・§c一部のオリジナル機能が使えない§r")
            sender.sendMessage("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。")
        }
        if (isManualCitizen) {
            sender.sendMessage("既に手動認証されているため、あなたは市民です！")
        } else {
            val ctx = luckPerms.contextManager.getContext(player)
            val linked = ctx.contains("discordsrv:linked", "true")
            val crafterRole = ctx.contains("discordsrv:role", "クラフター")
            val tick = record.getInt(PlayerDataKey.NEWCOMER_TIME)
            sender.sendMessage("§b§lクイック認証に必要な条件: ")
            sendMessage(sender, "Discord 連携済み", linked)
            sendMessage(sender, "クラフターロール付与済み", crafterRole)
            sendMessage(sender, "初参加から30分経過(残り" + tickToString(tick) + ")", tick == 0)
            sender.sendMessage(
                if (linked && crafterRole) "§b全ての条件を満たしているため、あなたは市民です！" else "§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。"
            )
        }
        sender.sendMessage("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！")
        return true
    }

    private fun sendMessage(player: Player, str: String, isSuccess: Boolean) {
        player.sendMessage((if (isSuccess) "§a✔ " else "§c✘ ") + str + "§r")
    }

    private fun tickToString(tick: Int): String {
        val elapsedTime = Ticks.toTime(tick).toInt()
        val elapsedTimeMinutes = elapsedTime / 60
        val elapsedTimeSeconds = elapsedTime % 60
        return if (elapsedTimeMinutes > 0) elapsedTimeMinutes.toString() + "分" + elapsedTimeSeconds + "秒" else elapsedTimeSeconds.toString() + "秒"
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return null
    }
}