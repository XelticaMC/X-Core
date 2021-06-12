package work.xeltica.craft.otanoshimiplugin.gui;

import org.bukkit.entity.Player;

public class BookButtonEventArgs {
    public BookButtonEventArgs(Player p) {
        player = p;
    }

    public Player getPlayer() {
        return player;
    }

    private Player player;
}
