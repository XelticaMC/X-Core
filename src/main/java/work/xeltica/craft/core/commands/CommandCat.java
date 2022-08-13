package work.xeltica.craft.core.commands;

import java.util.List;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.models.SoundPitch;
import work.xeltica.craft.core.modules.HintModule;
import work.xeltica.craft.core.modules.PlayerStoreModule;

/**
 * 猫モードを切り替えるコマンド
 * @author Xeltica
 */
public class CommandCat extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player sender, Command command, String label, String[] args) {
        final var record = PlayerStoreModule.open(sender);
        // 引数がない場合は現在のモードを表示
        if (args.length == 0) {
            final var mes = record.getBoolean(PlayerDataKey.CAT_MODE)
                ? "§aあなたはCATモードです。§r"
                : "§aあなたはCATモードではありません。§r";
            Gui.getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2, SoundPitch.F_1);
            sender.sendMessage(mes);
            return true;
        }

        // 引数がある場合はモードを設定
        final var arg = args[0].toLowerCase();
        switch (arg) {
            case "on" -> {
                record.set(PlayerDataKey.CAT_MODE, true);
                Gui.getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2, SoundPitch.F_2);
                sender.sendMessage("CATモードを§aオン§rにしました。");

                HintModule.achieve(sender, Hint.CAT_MODE);
            }
            case "off" -> {
                record.set(PlayerDataKey.CAT_MODE, false);
                Gui.getInstance().playSound(sender, Sound.ENTITY_CAT_AMBIENT, 2, SoundPitch.F_0);
                sender.sendMessage("CATモードを§cオフ§rにしました。");
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        return COMPLETE_LIST_ONOFF;
    }
}
