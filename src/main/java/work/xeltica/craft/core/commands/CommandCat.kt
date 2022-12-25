package work.xeltica.craft.core.commands

import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule.achieve
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.player.PlayerModule.open
import java.util.*

/**
 * 猫モードを切り替えるコマンド
 * @author Xeltica
 */
class CommandCat : CommandPlayerOnlyBase() {
    override fun execute(sender: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val record = open(sender)
        // 引数がない場合は現在のモードを表示
        if (args.isEmpty()) {
            val mes = if (record.getBoolean(PlayerDataKey.CAT_MODE)) "§aあなたはCATモードです。§r" else "§aあなたはCATモードではありません。§r"
            getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_1)
            sender.sendMessage(mes)
            return true
        }

        // 引数がある場合はモードを設定
        val arg = args[0].lowercase(Locale.getDefault())
        when (arg) {
            "on" -> {
                record[PlayerDataKey.CAT_MODE] = true
                getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_2)
                sender.sendMessage("CATモードを§aオン§rにしました。")
                achieve(sender, Hint.CAT_MODE)
            }

            "off" -> {
                record[PlayerDataKey.CAT_MODE] = false
                getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_0)
                sender.sendMessage("CATモードを§cオフ§rにしました。")
            }

            else -> {
                return false
            }
        }
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_ONOFF
    }
}