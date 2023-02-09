package work.xeltica.craft.core.modules.world

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase

/**
 * PvPの有効・無効を切り替えるコマンド
 * @author Lutica
 */
class CommandPvp : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) return false
        if (!args[0].equals("on", ignoreCase = true) && !args[0].equals("off", ignoreCase = true)) return false
        val world = player.world
        val flag = args[0].equals("on", ignoreCase = true)
        world.pvp = flag
        player.sendMessage(
            ChatColor.GOLD.toString() + String.format(
                "ワールド %s のPvPを%sしました",
                world.name,
                if (flag) "許可" else "禁止"
            )
        )
        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender, command: Command, label: String,
        args: Array<String>,
    ): List<String> {
        return COMPLETE_LIST_ONOFF
    }
}