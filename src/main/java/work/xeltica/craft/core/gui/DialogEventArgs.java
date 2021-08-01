package work.xeltica.craft.core.gui;

import org.bukkit.entity.Player;

/**
 * プレイヤーがUIダイアログのボタンを押したときに発生するイベントの引数。
 * @author Xeltica
 */
public class DialogEventArgs {
    public DialogEventArgs(Player p) {
        player = p;
    }

    public Player getPlayer() {
        return player;
    }

    private Player player;
}
