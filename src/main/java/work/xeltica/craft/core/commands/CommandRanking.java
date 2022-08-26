package work.xeltica.craft.core.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.api.commands.CommandBase;
import work.xeltica.craft.core.models.Ranking;
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
        final var id = sender instanceof Player p ? p.getUniqueId().toString() : null;
        
        try {
            switch (subCommand) {
                case "create" -> {
                    if (args.length < 3) {
                        sender.sendMessage("/ranking create <name> <displayName> [\"playermode\"]");
                        return true;
                    }
                    final var name = args[1].trim();
                    final var displayName = args[2].trim();
                    final var isPlayerMode = args.length >= 4 && args[3].equalsIgnoreCase("playermode");

                    if (api.has(name)) {
                        sender.sendMessage("既に存在します。");
                        return true;
                    }
                    api.create(name, displayName, isPlayerMode);
                    sender.sendMessage("ランキング " + name + "を" + (isPlayerMode ? "プレイヤーモードで" : "") + "作成しました。");
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
                    sender.sendMessage(api.delete(name) ? "削除に成功しました。" : "削除に失敗しました。");
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
                        sender.sendMessage(String.format("§6%d位:§a%s §b%s", i + 1, record.id(), record.score()));
                    }
                }
                case "list" -> {
                    final var list = api.getAll();
                    if (list.size() == 0) {
                        sender.sendMessage("一つもありません。");
                    } else {
                        list.stream()
                            .map(r -> String.format("%s §7(%s)", r.getName(), r.getDisplayName()))
                            .forEach(sender::sendMessage);
                    }
                }
                case "set" -> {
                    if (args.length != 3) {
                        sender.sendMessage("/ranking set <rankingName> <value>");
                        return true;
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。");
                        return true;
                    }
                    final var name = args[1].trim();
                    try {
                        final var value = Integer.parseInt(args[2]);
                        if (!api.has(name)) {
                            sender.sendMessage("ランキングが存在しません。");
                            return true;
                        }
                        final var ranking = api.get(name);
                        ranking.add(id, value);
                        sender.sendMessage("ランキングにレコードを追加しました。");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("値には数値のみ認められます。");
                    }
                }
                case "unset" -> {
                    if (args.length != 2) {
                        sender.sendMessage("/ranking unset <rankingName>");
                        return true;
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。");
                        return true;
                    }
                    final var name = args[1].trim();
                    try {
                        final var ranking = api.get(name);
                        ranking.remove(id);
                        sender.sendMessage("ランキングからレコードを削除しました。");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("値には数値のみ認められます。");
                    }
                }
                case "mode" -> {
                    if (args.length != 3) {
                        sender.sendMessage("/ranking mode <rankingName> <normal/time/point>");
                        return true;
                    }
                    final var name = args[1].trim();

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。");
                        return true;
                    }
                    final var mode = args[2].trim().toLowerCase();
                    if (!List.of("normal", "time", "point").contains(mode)) {
                        sender.sendMessage("モードには次のものを指定できます。");
                        sender.sendMessage("normal: 値を単位の無い普通の数値とみなす");
                        sender.sendMessage("  time: 値をミリ秒時間とみなす。カウンターと併用する場合はこちら");
                        sender.sendMessage(" point: 値を点数とみなす。");
                        return true;
                    }
                    api.get(name).setMode(mode);
                    sender.sendMessage("モードを設定しました。");
                }
                case "hologram" -> {
                    if (args.length < 3) {
                        sender.sendMessage("/ranking hologram <name> <sub-commands>");
                        return true;
                    }
                    if (id == null) {
                        sender.sendMessage("プレイヤーが実行してください。");
                        return true;
                    }
                    final var player = (Player)sender;
                    final var name = args[1];

                    if (!api.has(name)) {
                        sender.sendMessage("存在しません。");
                        return true;
                    }
                    final var data = api.get(name);
                    final var hSubCommand = args[2].toLowerCase();

                    switch (hSubCommand) {
                        case "spawn" -> data.setHologram(player.getLocation().add(0, 3, 0), data.getHologramHidden());
                        case "despawn" -> data.setHologram(null, data.getHologramHidden());
                        case "obfuscate" -> data.setHologram(data.getHologramLocation(), true);
                        case "deobfuscate" -> data.setHologram(data.getHologramLocation(), false);
                    }
                }
                default -> {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("IOエラーにより失敗しました。");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            final var commands = Arrays.asList("create", "delete", "query", "list", "set", "unset", "hologram", "mode");
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[0], commands, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            if (!Arrays.asList("delete", "query", "set", "unset", "hologram", "mode").contains(args[0])) return COMPLETE_LIST_EMPTY;
            final var store = RankingStore.getInstance();
            final var rankings = store.getAll().stream().map(Ranking::getName).toList();
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[1], rankings, completions);
            Collections.sort(completions);
            return rankings;
        } else if (args.length == 3) {
            switch (args[0]) {
                case "mode" -> {
                    final var modes = Arrays.asList("normal", "time", "point");
                    final var completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches(args[2], modes, completions);
                    Collections.sort(completions);
                    return completions;
                }
                case "hologram" -> {
                    final var subCommands = Arrays.asList("spawn", "despawn", "obfuscate", "deobfuscate");
                    final var completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches(args[2], subCommands, completions);
                    Collections.sort(completions);
                    return completions;
                }
            }
        }
        return COMPLETE_LIST_EMPTY;
    }
}

