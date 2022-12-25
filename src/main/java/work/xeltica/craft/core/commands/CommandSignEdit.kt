package work.xeltica.craft.core.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;

/**
 * 看板を編集するコマンド
 * @author Xeltica
 */
public class CommandSignEdit extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        final var block = player.getTargetBlock(null, 5);
        if (!Tag.SIGNS.isTagged(block.getType())) {
            player.sendMessage(ChatColor.RED + "変更する対象の看板を見てください");
            return true;
        }
        final var state = (Sign)block.getState();
        try {
            final var index = Integer.parseInt(args[0]);
            if (index < 0 || index > 3) {
                player.sendMessage(ChatColor.RED + "行番号には0,1,2,3を指定してください");
                return true;
            }
            final var l = new LinkedList<>(Arrays.asList(args));
            l.remove(0);
            final var line = String.join(" ", l);
            state.line(index, Component.text(line));
            // Spigot イベントを発行し、他のプラグインにキャンセルされたらやめる
            final var e = new SignChangeEvent(block, player, state.lines());
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isCancelled()) {
                state.update();
                player.sendMessage("看板の" + index + "行目を「" + line + "」と書き換えました");
            } else {
                player.sendMessage("何らかの理由でこの看板は編集できません");
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        final var errorNonSign = List.of("変更する対象の看板を見てください");
        final var errorOutOfBounds = List.of("第一引数は0,1,2,3のいずれかにしてください");

        if (args.length < 2) return COMMANDS;

        if (commandSender instanceof Player player) {
            final var block = player.getTargetBlock(null, 5);
            if (!Tag.SIGNS.isTagged(block.getType())) return errorNonSign;
            if (!List.of("0", "1", "2", "3").contains(args[0])) return errorOutOfBounds;
            final var n = Integer.parseInt(args[0]);
            
            final var state = (Sign)block.getState();
            return List.of(PlainTextComponentSerializer.plainText().serialize(state.line(n)));
        } else {
            return COMPLETE_LIST_EMPTY;
        }
    }

    private static final List<String> COMMANDS = List.of("0", "1", "2", "3");
}
