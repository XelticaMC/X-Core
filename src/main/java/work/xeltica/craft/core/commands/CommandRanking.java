package work.xeltica.craft.core.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import work.xeltica.craft.core.stores.RankingStore;

/**
 * ランキングを操作するコマンド
 * @author Xeltica
 */
public class CommandRanking extends CommandBase {
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        final var subCommand = args[0].toLowerCase();
        final var api = RankingStore.getInstance();

        switch (subCommand) {
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage("/ranking create <name> <displayName> [\"playermode\"]");
                    return true;
                }
                final var name = args[1].trim();
                final var displayName = args[2].trim();
                final var isPlayerMode = args.length >= 3 && args[3].equalsIgnoreCase("playermode");

                if (api.has(name)) {
                    sender.sendMessage("既に存在します。");
                    return true;
                }
                try {
                    var ranking = api.create(name, displayName);
                    ranking.setIsPlayerMode(isPlayerMode);
                } catch (IOException e) {
                    sender.sendMessage("IOエラーにより失敗しました。");
                }
            }
            case "delete" -> {
                if (args.length != 2) {
                    sender.sendMessage("/ranking delete <name>");
                    return true;
                }
                final var name = args[1].trim();

                if (!api.has(name)) {
                    sender.sendMessage("存在しません。");
                    return true;
                }
                try {
                    sender.sendMessage(api.delete(name) ? "成功しました。" : "失敗しました。");
                } catch (IOException e) {
                    sender.sendMessage("IOエラーにより失敗しました。");
                }
            }
            case "query" -> {
                if (args.length != 2) {
                    sender.sendMessage("/ranking query <name>");
                    return true;
                }
                final var name = args[1].trim();

                if (!api.has(name)) {
                    sender.sendMessage("存在しません。");
                    return true;
                }
                final var ranking = api.get(name).queryRanking();
                for (var i = 0; i < ranking.length; i++) {
                    final var record = ranking[i];
                    sender.sendMessage(String.format("§6%d位:§a%s &b%d点", i + 1, record.id(), record.score()));
                }
            }
            case "list" -> {
                final var list = api.getAll();
                if (list.size() == 0) {
                    sender.sendMessage("一つもありません。");
                } else {
                    list.stream()
                        .map(r -> String.format("%s §7(%s)", r.getName(), r.getDisplayName()))
                        .forEach(r -> sender.sendMessage(r));
                }
            }
            default -> {
                return false;
            }
        }

        return true;
    }

}

