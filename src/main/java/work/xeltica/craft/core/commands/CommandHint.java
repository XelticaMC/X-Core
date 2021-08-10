package work.xeltica.craft.core.commands;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        final var subCommand = args.length > 0 ? args[0] : null;
        final var store = HintStore.getInstance();
        final var hints = Stream.of(Hint.values());
        if (subCommand != null) {
            // 指定されたIDのヒントの詳細をプレイヤーに表示

            final var optionalHint = hints.filter(h -> subCommand.equalsIgnoreCase(h.name())).findFirst();
            if (!optionalHint.isPresent()) {
                player.sendMessage("ヒントが存在しません。");
                return true;
            }

            final var hint = optionalHint.get();
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

            final var items = hints.map(h -> {
                final var isAchieved = store.hasAchieved(player, h);
                final var isQuest = h.getPower() > 0;

                final var name = h.getName() + (isQuest ? (" (" + h.getPower() + "EP)") : "");
                final var icon = !isQuest ? Material.NETHER_STAR : isAchieved ? Material.GOLD_BLOCK : Material.GOLD_NUGGET;
                final Consumer<MenuItem> onClick = (m) -> player.performCommand("hint " + h.name());

                return new MenuItem(name, onClick, icon, null, 1, isAchieved);
            }).toList();

            Gui.getInstance().openMenu(player, "ヒント", items);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label,
            String[] args) {
        return Stream.of(Hint.values()).map(h -> h.toString()).toList();
    }
}
