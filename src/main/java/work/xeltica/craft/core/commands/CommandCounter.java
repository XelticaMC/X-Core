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
                case "register" -> {
                    if (args.length < 2) return ui.error(player, "/counter register <name> [\"daily\"]");
                    final var name = args[1];
                    final var isDaily = args.length == 3 && "daily".equalsIgnoreCase(args[2]);
                    record.set(PlayerDataKey.COUNTER_REGISTER_MODE, true, false);
                    record.set(PlayerDataKey.COUNTER_REGISTER_NAME, name, false);
                    record.set(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, isDaily, false);
                    pstore.save();
                                    
                    player.sendMessage("カウンター登録モードは有効。カウンターの始点にする感圧板を右クリックかタップしてください。");
                    player.sendMessage("キャンセルする際には、 /counter cancel を実行します。");
                }
                // counter cancel
                case "cancel" -> {
                    record.delete(PlayerDataKey.COUNTER_REGISTER_MODE, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_NAME, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, false);
                    record.delete(PlayerDataKey.COUNTER_REGISTER_LOCATION, false);
                    pstore.save();

                    player.sendMessage("カウンター登録モードを無効化し、キャンセルしました。");
                }
                // counter unregister <name>
                case "unregister" -> {
                    if (args.length != 2) return ui.error(player, "/counter unregister <name>");
                    final var name = args[1];
                    final var data = store.getByName(name);
                    if (data == null) {
                        return ui.error(player, name + "という名前のカウンターは存在しません。");
                    }

                    store.remove(data);
                    player.sendMessage(name + "を登録解除しました。");
                }
                // counter bind <playerType> <rankingName>
                case "bind" -> {
                    return ui.error(player, "未実装");
                }
                // counter info <name>
                case "info" -> {
                    if (args.length != 2) return ui.error(player, "/counter info <name>");
                    final var name = args[1];
                    final var data = store.getByName(name);
                    player.sendMessage("名前: " + name);
                    player.sendMessage("ID: " + store.getIdOf(data));
                    player.sendMessage("始点: " + data.getLocation1().toString());
                    player.sendMessage("終点: " + data.getLocation2().toString());
                    player.sendMessage("1日1回かどうか: " + data.isDaily());
                    player.sendMessage("紐付いたランキングID: " + data.getRankingId());
                }
                // counter list
                case "list" -> {
                    store.getCounters().forEach(c -> player.sendMessage("* " + c.getName()));
                }
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
