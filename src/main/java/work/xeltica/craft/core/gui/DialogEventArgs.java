package work.xeltica.craft.core.gui;

import org.bukkit.entity.Player;

public class DialogEventArgs {
    public DialogEventArgs(Player p) {
        player = p;
    }

    public Player getPlayer() {
        return player;
    }

    private Player player;
}
