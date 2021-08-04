package work.xeltica.craft.core.commands;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;

/**
 * ヒントアプリを開くコマンド
 * @author Xeltica
 */
public class CommandHint extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var subCommand = args.length > 0 ? args[0] : null;
        var store = HintStore.getInstance();
        var hints = Stream.of(Hint.values());
        if (subCommand != null) {
            // 指定されたIDのヒントの詳細をプレイヤーに表示

            var optionalHint = hints.filter(h -> subCommand.equalsIgnoreCase(h.name())).findFirst();
            if (!optionalHint.isPresent()) {
                player.sendMessage("ヒントが存在しません。");
                return true;
            }

            var hint = optionalHint.get();
            var content = hint.getDescription();
            if (hint.getPower() > 0) {
                content += "\n\n" + "§a§l報酬: §r§d" + hint.getPower() + " エビパワー";
                if (store.hasAchieved(player, hint)) {
                    content += "\n" + "§6§o✧達成済み✧";
                }
            }

            Gui.getInstance().openDialog(player, "§l" + hint.getName() + "§r", content, (d) -> {
                player.performCommand("hint");
            });
        } else {
            // ヒント一覧をプレイヤーに表示

            var items = hints.map(h -> {
                var isAchieved = store.hasAchieved(player, h);
                var isQuest = h.getPower() > 0;

                var name = h.getName() + (isQuest ? (" (" + h.getPower() + "EP)") : "");
                var icon = !isQuest ? Material.NETHER_STAR : isAchieved ? Material.GOLD_BLOCK : Material.GOLD_NUGGET;
                Consumer<MenuItem> onClick = (m) -> player.performCommand("hint " + h.name());

                return new MenuItem(name, onClick, icon, null, 1, isAchieved);
            }).toList();

            Gui.getInstance().openMenu(player, "ヒント", items);
        }
        return true;
    }
}
