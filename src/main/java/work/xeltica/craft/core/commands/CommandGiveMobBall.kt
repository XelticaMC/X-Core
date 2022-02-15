package work.xeltica.craft.core.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.MobBallStore

class CommandGiveMobBall : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val name = if (args.isNotEmpty()) args[0] else null
        val p = if (name == null) player else Bukkit.getPlayer(name)
        if (p == null) {
            player.sendMessage(ChatColor.RED.toString() + "そのようなプレイヤーはいません")
            return true
        }
        val amount = if (args.size > 1) Integer.parseInt(args[1]) else 1
        // TODO: 別のタイプのボールを追加するときに外す
        // val type = if (args.size > 2) args[2] else "normal"

        p.inventory.addItem(MobBallStore.getInstance().createMobBall(amount))
        p.sendMessage("${p.name}にモブボールを${amount}個付与しました。")
        p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return super.onTabComplete(commandSender, command, label, args)
    }

}