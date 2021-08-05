package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.PlayerStore;

/**
 * 猫モードを切り替えるコマンド
 * @author Xeltica
 */
public class CommandCat extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player sender, Command command, String label, String[] args) {
        final var record = PlayerStore.getInstance().open(sender);
        // 引数がない場合は現在のモードを表示
        if (args.length == 0) {
            final var mes = record.getBoolean(PlayerDataKey.CAT_MODE)
                ? "§aあなたはCATモードです。§r"
                : "§aあなたはCATモードではありません。§r";
            sender.sendMessage(mes);
            return true;
        }

        // 引数がある場合はモードを設定
        final var arg = args[0].toLowerCase();
        switch (arg) {
            case "on" -> {
                record.set(PlayerDataKey.CAT_MODE, true);
                sender.sendMessage("CATモードを§aオン§rにしました。");

                HintStore.getInstance().achieve(sender, Hint.CAT_MODE);
            }
            case "off" -> {
                record.set(PlayerDataKey.CAT_MODE, false);
                sender.sendMessage("CATモードを§cオフ§rにしました。");
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
