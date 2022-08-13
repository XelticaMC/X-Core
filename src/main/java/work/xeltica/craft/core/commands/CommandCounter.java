package work.xeltica.craft.core.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.CounterData;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.models.Ranking;
import work.xeltica.craft.core.plugins.CounterModule;
import work.xeltica.craft.core.modules.PlayerStoreModule;
import work.xeltica.craft.core.stores.RankingStore;

/**
 * トロッコを出現させるコマンド
 * @author Xeltica
 */
public class CommandCounter extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        final var subCommand = args[0].toLowerCase();
        final var record = PlayerStoreModule.open(player);
        final var ui = Gui.getInstance();

        try {
            switch (subCommand) {
                // 登録を行います。
                // counter register
                case "register" -> {
                    if (args.length < 2) return ui.error(player, "/counter register <name> [\"daily\"]");
                    final var name = args[1];
                    final var isDaily = args.length == 3 && "daily".equalsIgnoreCase(args[2]);
                    record.set(PlayerDataKey.COUNTER_REGISTER_MODE, true);
                    record.set(PlayerDataKey.COUNTER_REGISTER_NAME, name);
                    record.set(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, isDaily);
                    PlayerStoreModule.save();
                                    
                    player.sendMessage("カウンター登録モードは有効。カウンターの始点にする感圧板をクリックかタップしてください。");
                    player.sendMessage("キャンセルする際には、 /counter cancel を実行します。");
                }

                // カウンター登録をキャンセルします。
                // counter cancel
                case "cancel" -> {
                    record.delete(PlayerDataKey.COUNTER_REGISTER_MODE);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_NAME);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_IS_DAILY);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_LOCATION);
                    PlayerStoreModule.save();

                    player.sendMessage("カウンター登録モードを無効化し、キャンセルしました。");
                }

                // カウンターを登録解除します。
                // counter unregister <name>
                case "unregister" -> {
                    if (args.length != 2) return ui.error(player, "/counter unregister <name>");
                    final var name = args[1];
                    final var data = CounterModule.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }

                    CounterModule.remove(data);
                    player.sendMessage(name + "を登録解除しました。");
                }

                // カウンターをランキングに紐付けます。
                // counter bind <name> <playerType> [rankingName]
                case "bind" -> {
                    if (args.length != 4) return ui.error(player, "/counter bind <name> <all/java/bedrock/uwp/phone> <rankingName>");
                    final var name = args[1].toLowerCase();
                    final var playerType = args[2].toLowerCase();
                    final var rankingName = args[3];

                    final var data = CounterModule.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }
                    
                    switch (playerType) {
                        case "all" -> {
                            data.setJavaRankingId(rankingName);
                            data.setBedrockRankingId(rankingName);
                        }
                        case "java" -> data.setJavaRankingId(rankingName);
                        case "bedrock" -> data.setBedrockRankingId(rankingName);
                        case "uwp" -> data.setUwpRankingId(rankingName);
                        case "phone" -> data.setPhoneRankingId(rankingName);
                    }
                    CounterModule.update(data);
                    player.sendMessage("カウンターをランキングに紐付けました。");
                }

                // カウンターをランキングから紐付け解除します。
                // counter unbind <name> <playerType>
                case "unbind" -> {
                    if (args.length != 3) return ui.error(player, "/counter unbind <name> <all/java/bedrock/uwp/phone>");
                    final var name = args[1].toLowerCase();
                    final var playerType = args[2].toLowerCase();

                    final var data = CounterModule.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }
                    
                    switch (playerType) {
                        case "all" -> {
                            data.setJavaRankingId(null);
                            data.setBedrockRankingId(null);
                        }
                        case "java" -> data.setJavaRankingId(null);
                        case "bedrock" -> data.setBedrockRankingId(null);
                        case "uwp" -> data.setUwpRankingId(null);
                        case "phone" -> data.setPhoneRankingId(null);
                    }
                    CounterModule.update(data);
                    player.sendMessage("カウンターをランキングから解除ました。");
                }

                // カウンターの情報を開示します。
                // counter info <name>
                case "info" -> {
                    if (args.length != 2) return ui.error(player, "/counter info <name>");
                    final var name = args[1];
                    final var data = CounterModule.get(name);
                    player.sendMessage("名前: " + name);
                    player.sendMessage("始点: " + data.getLocation1().toString());
                    player.sendMessage("終点: " + data.getLocation2().toString());
                    player.sendMessage("1日1回かどうか: " + data.isDaily());
                    player.sendMessage("紐付いたランキングID: ");
                    player.sendMessage(" Java: " + data.getJavaRankingId());
                    player.sendMessage(" Bedrock: " + data.getBedrockRankingId());
                    player.sendMessage(" UWP: " + data.getUwpRankingId());
                    player.sendMessage(" Phone: " + data.getPhoneRankingId());
                }

                // カウンターのプレイ済み履歴を指定したプレイヤーか全プレイヤー分削除します。
                // counter resetdaily [player]
                case "resetdaily" -> {
                    if (args.length != 2) {
                        CounterModule.resetAllPlayersPlayedLog();
                        player.sendMessage("全プレイヤーのプレイ済み履歴を削除しました。");
                    } else {
                        final var name = args[1];
                        PlayerStoreModule.open(Bukkit.getPlayerUniqueId(name)).delete(PlayerDataKey.PLAYED_COUNTER);
                        player.sendMessage("そのプレイヤーのプレイ済み履歴を削除しました。");
                    }
                }

                // カウンターを一覧表示します。
                // counter list
                case "list" -> {
                    final var list = CounterModule.getCounters();
                    if (list.size() == 0) {
                        ui.error(player, "カウンターはまだ作成されていません。");
                    } else {
                        player.sendMessage("合計: " + list.size());
                        list.forEach(c -> player.sendMessage("* " + c.getName()));
                    }
                }

                // カウンターをデイリーイベント仕様にするかどうか設定する
                // 現状は全部デイリーイベント仕様である
                // counter setisdaily <true/false>
                case "setisdaily" -> {
                    return ui.error(player, "未実装");
                }
                default -> { return false; }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ui.error(player, "§cIO エラーが発生したために処理を続行できませんでした。");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) return COMPLETE_LIST_EMPTY;
        final var subcommand = args[0].toLowerCase();
        if (args.length == 1) {
            final var commands = Arrays.asList("register", "unregister", "cancel", "bind", "unbind", "info", "list", "resetdaily");
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(subcommand, commands, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            switch (subcommand) {
                case "unregister", "info", "bind", "unbind" -> {
                    return CounterModule.getCounters().stream().map(CounterData::getName).toList();
                }
            }
        } else if (args.length == 3) {
            switch (subcommand) {
                case "bind", "unbind" -> {
                    final var playerType = args[2].toLowerCase();
                    final var types = Arrays.asList("all", "java", "bedrock", "uwp", "phone");
                    final var completions = new ArrayList<String>();
                    StringUtil.copyPartialMatches(playerType, types, completions);
                    Collections.sort(completions);
                    return completions;
                }
            }
        } else if (args.length == 4) {
            if ("bind".equals(subcommand)) {
                final var store = RankingStore.getInstance();
                final var rankings = store.getAll().stream().map(Ranking::getName).toList();
                final var completions = new ArrayList<String>();
                StringUtil.copyPartialMatches(args[3], rankings, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return COMPLETE_LIST_EMPTY;
    }
}
