package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.stores.PlayerStore;

public class CommandLive extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length == 0 || (!args[0].equals("on") && !args[0].equals("off"))) return false;

        var isLiveMode = args[0].equals("on");
        var store = PlayerStore.getInstance();

        if (store.isLiveMode(player) == isLiveMode) {
            return Gui.getInstance().error(player, "既に" + (isLiveMode ? "オン" : "オフ") + "です");
        }

        store.setLiveMode(player, isLiveMode);
        return true;
    }

    
}
