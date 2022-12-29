package work.xeltica.craft.core.commands

import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.gui.SoundPitch
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule.achieve
import work.xeltica.craft.core.modules.player.PlayerDataKey
import java.util.*

/**
 * 猫モードを切り替えるコマンド
 * @author Xeltica
 */
class CommandCat : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val record = PlayerStore.open(player)
        // 引数がない場合は現在のモードを表示
        if (args.isEmpty()) {
            val mes = if (record.getBoolean(PlayerDataKey.CAT_MODE)) "§aあなたはCATモードです。§r" else "§aあなたはCATモードではありません。§r"
            getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_1)
            player.sendMessage(mes)
            return true
        }

        // 引数がある場合はモードを設定
        val arg = args[0].lowercase(Locale.getDefault())
        when (arg) {
            "on" -> {
                record[PlayerDataKey.CAT_MODE] = true
                getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_2)
                player.sendMessage("CATモードを§aオン§rにしました。")
                achieve(player, Hint.CAT_MODE)
            }

            "off" -> {
                record[PlayerDataKey.CAT_MODE] = false
                getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_0)
                player.sendMessage("CATモードを§cオフ§rにしました。")
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