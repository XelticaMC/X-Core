package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.modules.UIModule;

/**
 * Java版で看板を使用したダイアログのボタンを
 * クリックしたときに内部的に送られるコマンド
 * プレイヤーが使用することは想定していない
 * @author Xeltica
 */
public class CommandXCoreGuiEvent extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        UIModule.getInstance().handleCommand(args[0]);
        return true;
    }
    
}
