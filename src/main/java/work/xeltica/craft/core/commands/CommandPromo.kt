package work.xeltica.craft.core.commands

import net.luckperms.api.LuckPerms
import work.xeltica.craft.core.stores.PlayerStore
import net.luckperms.api.query.QueryOptions
import net.luckperms.api.model.group.Group
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.utils.Ticks

/**
 * 市民システムの情報表示コマンド
 * @author Xeltica
 */
class CommandPromo : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        val luckPerms = provider!!.provider
        val lpUser = luckPerms.getPlayerAdapter(Player::class.java).getUser(player)
        val store = PlayerStore.getInstance()
        val record = store.open(player)
        val isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream()
            .anyMatch { g: Group -> g.name == "citizen" }
        if (!store.isCitizen(player)) {
            player.sendMessage("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。")
            player.sendMessage("§b市民§rにならなくても基本的なプレイはできますが、")
            player.sendMessage("・§c一部ブロックが使えない§r")
            player.sendMessage("・§c一部のオリジナル機能が使えない§r")
            player.sendMessage("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。§b市民§rに昇格する方法の一つに、クイック認証があります。")
        }
        if (isManualCitizen) {
            player.sendMessage("既に手動認証されているため、あなたは市民です！")
        } else {
            val ctx = luckPerms.contextManager.getContext(player)
            val linked = ctx.contains("discordsrv:linked", "true")
            val crafterRole = ctx.contains("discordsrv:role", "クラフター")
            val citizenRole = ctx.contains("discordsrv:role", "市民")
            val tick = record.getInt(PlayerDataKey.NEWCOMER_TIME)
            player.sendMessage("§b§lクイック認証に必要な条件: ")
            sendMessage(player, "Discord 連携済み", linked)
            sendMessage(player, "クラフターロール付与済み", crafterRole)
            sendMessage(player, "市民ロール付与済み", citizenRole)
            sendMessage(player, "初参加から30分経過(残り" + tickToString(tick) + ")", tick == 0)
            player.sendMessage(
                if (linked && crafterRole && citizenRole) "§b全ての条件を満たしているため、あなたは市民です！" else "§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。"
            )
            if (!(linked && crafterRole && citizenRole)) {
                player.sendMessage("クイック認証が面倒であれば§6§l手動認証§rもできます。")
            }
        }
        player.sendMessage("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！")
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

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        return COMPLETE_LIST_EMPTY
    }
}