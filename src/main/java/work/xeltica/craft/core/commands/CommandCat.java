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
        if (args.length > 0) {
            final var arg = args[0];
            if ("on".equals(arg)) {
                record.set(PlayerDataKey.CAT_MODE, true);
                HintStore.getInstance().achieve(sender, Hint.CAT_MODE);
                sender.sendMessage("CATモードを§aオン§rにしました。");
            } else if ("off".equals(arg)) {
                record.set(PlayerDataKey.CAT_MODE, false);
                sender.sendMessage("CATモードを§cオフ§rにしました。");
            } else {
                return false;
            }
        } else {
            final var mes = record.getBoolean(PlayerDataKey.CAT_MODE) ? "§aあなたはCATモードです。§r" : "§aあなたはCATモードではありません。§r";
            sender.sendMessage(mes);
        }
        return true;
    }

}
