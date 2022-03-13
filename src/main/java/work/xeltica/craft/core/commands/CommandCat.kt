package work.xeltica.craft.core.commands

import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import work.xeltica.craft.core.COMPLETE_LIST_ONOFF
import work.xeltica.craft.core.stores.PlayerStore
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.models.SoundPitch
import java.util.Locale
import work.xeltica.craft.core.stores.HintStore

/**
 * 猫モードを切り替えるコマンド
 * @author Xeltica
 */
class CommandCat : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val record = PlayerStore.getInstance().open(player)
        // 引数がない場合は現在のモードを表示
        if (args.isEmpty()) {
            val mes = if (record.getBoolean(PlayerDataKey.CAT_MODE)) "§aあなたはCATモードです。§r" else "§aあなたはCATモードではありません。§r"
            Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_1)
            player.sendMessage(mes)
            return true
        }

        // 引数がある場合はモードを設定
        val arg = args[0].lowercase(Locale.getDefault())
        when (arg) {
            "on" -> {
                record[PlayerDataKey.CAT_MODE] = true
                Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_2)
                player.sendMessage("CATモードを§aオン§rにしました。")
                HintStore.getInstance().achieve(player, Hint.CAT_MODE)
            }
            "off" -> {
                record[PlayerDataKey.CAT_MODE] = false
                Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_0)
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