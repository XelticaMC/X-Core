package work.xeltica.craft.core.modules.cat

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.SoundPitch

/**
 * 猫モードを切り替えるコマンド
 * @author Lutica
 */
class CommandCat : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        // 引数がない場合は現在のモードを表示
        if (args.isEmpty()) {
            val mes =
                if (CatModule.isCat(player)) {
                    "${ChatColor.GREEN}あなたはCATモードです。${ChatColor.RESET}"
                } else {
                    "${ChatColor.GREEN}あなたはCATモードではありません。${ChatColor.RESET}"
                }
            Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_1)
            player.sendMessage(mes)
            return true
        }

        // 引数がある場合はモードを設定
        when (args[0].lowercase()) {
            "on" -> CatModule.setCat(player, true)
            "off" -> CatModule.setCat(player, false)
            else -> return false
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_ONOFF
    }
}