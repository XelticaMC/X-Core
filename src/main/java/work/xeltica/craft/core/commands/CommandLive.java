package work.xeltica.craft.core.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.stores.PlayerStore;

/**
 * 配信モードを切り替えるコマンド
 * @author Xeltica
 */
public class CommandLive extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length == 0 || (!args[0].equals("on") && !args[0].equals("off"))) return false;

        final var isLiveMode = args[0].equals("on");
        final var store = PlayerStore.getInstance();

        if (store.isLiveMode(player) == isLiveMode) {
            return Gui.getInstance().error(player, "既に" + (isLiveMode ? "オン" : "オフ") + "です");
        }

        store.setLiveMode(player, isLiveMode);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 1) return COMPLETE_LIST_EMPTY;
        return COMPLETE_LIST_ONOFF;
    }
}
