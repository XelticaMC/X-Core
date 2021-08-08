package work.xeltica.craft.core.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.CounterStore;
import work.xeltica.craft.core.stores.PlayerStore;

/**
 * トロッコを出現させるコマンド
 * @author Xeltica
 */
public class CommandCounter extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        final var subCommand = args[0].toLowerCase();
        final var store = CounterStore.getInstance();
        final var pstore = PlayerStore.getInstance();
        final var record = pstore.open(player);
        final var ui = Gui.getInstance();

        try {
            switch (subCommand) {
                // 登録を行います。
                // counter register
                case "register" -> {
                    if (args.length < 2) return ui.error(player, "/counter register <name> [\"daily\"]");
                    final var name = args[1];
                    final var isDaily = args.length == 3 && "daily".equalsIgnoreCase(args[2]);
                    record.set(PlayerDataKey.COUNTER_REGISTER_MODE, true, false);
                    record.set(PlayerDataKey.COUNTER_REGISTER_NAME, name, false);
                    record.set(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, isDaily, false);
                    pstore.save();
                                    
                    player.sendMessage("カウンター登録モードは有効。カウンターの始点にする感圧板をクリックかタップしてください。");
                    player.sendMessage("キャンセルする際には、 /counter cancel を実行します。");
                }

                // カウンター登録をキャンセルします。
                // counter cancel
                case "cancel" -> {
                    record.delete(PlayerDataKey.COUNTER_REGISTER_MODE, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_NAME, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_LOCATION, false);
                    pstore.save();

                    player.sendMessage("カウンター登録モードを無効化し、キャンセルしました。");
                }

                // カウンターを登録解除します。
                // counter unregister <name>
                case "unregister" -> {
                    if (args.length != 2) return ui.error(player, "/counter unregister <name>");
                    final var name = args[1];
                    final var data = store.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }

                    store.remove(data);
                    player.sendMessage(name + "を登録解除しました。");
                }

                // カウンターをランキングに紐付けます。
                // counter bind <name> <playerType> [rankingName]
                case "bind" -> {
                    if (args.length != 4) return ui.error(player, "/counter bind <name> <all/java/bedrock/uwp/phone> <rankingName>");
                    final var name = args[1].toLowerCase();
                    final var playerType = args[2].toLowerCase();
                    final var rankingName = args[3];

                    final var data = store.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }
                    
                    switch (playerType) {
                        case "all" -> {
                            data.setJavaRankingId(rankingName);
                            data.setBedrockRankingId(rankingName);
                        }
                        case "java" -> {
                            data.setJavaRankingId(rankingName);
                        }
                        case "bedrock" -> {
                            data.setBedrockRankingId(rankingName);
                        }
                        case "uwp" -> {
                            data.setUwpRankingId(rankingName);
                        }
                        case "phone" -> {
                            data.setPhoneRankingId(rankingName);
                        }
                    }
                    store.update(data);
                    player.sendMessage("カウンターをランキングに紐付けました。");
                }

                // カウンターをランキングから紐付け解除します。
                // counter unbind <name> <playerType>
                case "unbind" -> {
                    if (args.length != 3) return ui.error(player, "/counter unbind <name> <all/java/bedrock/uwp/phone>");
                    final var name = args[1].toLowerCase();
                    final var playerType = args[2].toLowerCase();

                    final var data = store.get(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }
                    
                    switch (playerType) {
                        case "all" -> {
                            data.setJavaRankingId(null);
                            data.setBedrockRankingId(null);
                        }
                        case "java" -> {
                            data.setJavaRankingId(null);
                        }
                        case "bedrock" -> {
                            data.setBedrockRankingId(null);
                        }
                        case "uwp" -> {
                            data.setUwpRankingId(null);
                        }
                        case "phone" -> {
                            data.setPhoneRankingId(null);
                        }
                    }
                    store.update(data);
                    player.sendMessage("カウンターをランキングから解除ました。");
                }

                // カウンターの情報を開示します。
                // counter info <name>
                case "info" -> {
                    if (args.length != 2) return ui.error(player, "/counter info <name>");
                    final var name = args[1];
                    final var data = store.get(name);
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

                // カウンターのプレイ済み履歴を全プレイヤー分削除します。
                // counter resetdaily
                case "resetdaily" -> {
                    store.resetAllPlayersPlayedLog();
                    player.sendMessage("全プレイヤーのプレイ済み履歴を削除しました。");
                }

                // カウンターを一覧表示します。
                // counter list
                case "list" -> {
                    final var list = store.getCounters();
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
}
