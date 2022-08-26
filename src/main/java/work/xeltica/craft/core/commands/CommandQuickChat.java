package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.api.commands.CommandBase;
import work.xeltica.craft.core.stores.QuickChatStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author raink1208
 */
public class CommandQuickChat extends CommandBase {
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        final var subCmd = args[0].toLowerCase();
        final var store = QuickChatStore.getInstance();

        switch (subCmd) {
            case "register" -> {
                if (args.length < 3) return false;
                if (store.register(args[1], args[2])) {
                    sender.sendMessage(args[1] + "に" + args[2] + "を登録しました");
                } else {
                    sender.sendMessage("既に" + args[1] + "は登録されています");
                }
            }
            case "unregister" -> {
                if (args.length < 2) return false;
                if (store.unregister(args[1])) {
                    sender.sendMessage(args[1] + "を削除しました");
                } else {
                    sender.sendMessage(args[1] + "は存在しません");
                }
            }
            case "list" -> {
                sender.sendMessage("===== Quick Chat List =====");
                for (String prefix: store.getAllPrefix()) {
                    sender.sendMessage(prefix + " : " + store.getMessage(prefix));
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) return COMPLETE_LIST_EMPTY;
        final var subCmd = args[0].toLowerCase();
        if (args.length == 1) {
            final var commands = Arrays.asList("register", "unregister", "list");
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(subCmd, commands, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            if (subCmd.equals("unregister")) {
                final var store = QuickChatStore.getInstance();
                final var completions = new ArrayList<String>();
                StringUtil.copyPartialMatches(args[1], store.getAllPrefix(), completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return COMPLETE_LIST_EMPTY;
    }
}
