package work.xeltica.craft.otanoshimiplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class CommandPromo extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (player.hasPermission("otanoshimi.citizen")) {
            player.sendMessage("あなたは既に市民です！");
            return true;
        }
        player.sendMessage("市民に昇格すると、全ての機能が使えるようになります！");
        player.sendMessage("市民への昇格を行うためには、https://craft.xeltica.work/docs/citizen を確認してください！");
        return true;
    }
}
