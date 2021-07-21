package work.xeltica.craft.core.commands;

import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;

public class CommandHint extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var subCommand = args.length > 0 ? args[0] : null;
        var store = HintStore.getInstance();
        var hints = Stream.of(Hint.values());
        if (subCommand != null) {
            var optionalHint = hints.filter(h -> subCommand.equalsIgnoreCase(h.name())).findFirst();
            if (!optionalHint.isPresent()) {
                player.sendMessage("ヒントが存在しません。");
                return true;
            }
            var hint = optionalHint.get();
            var content = hint.getDescription() + "\n\n" + "§a§l報酬: §r§d" + hint.getPower() + " エビパワー";
            if (store.hasAchieved(player, hint)) {
                content += "\n" + "§6§o✧達成済み✧";
            }
            Gui.getInstance().openDialog(player, "§l" + hint.getName() + "§r", content, (d) -> {
                player.performCommand("hint");
            });
        } else {
            var items = hints.map(h -> new MenuItem(h.getName() + " (" + h.getPower() + "EP)", (m) -> {
                player.performCommand("hint " + h.name());
            }, store.hasAchieved(player, h) ? Material.GOLD_BLOCK : Material.GOLD_NUGGET)).toList();
            Gui.getInstance().openMenu(player, "ヒント", items);
        }
        return true;
    }
}
