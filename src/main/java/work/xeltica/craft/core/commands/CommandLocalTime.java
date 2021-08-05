package work.xeltica.craft.core.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * 現在いるワールドのみの時間を操作するコマンド
 * @author Xeltica
 */
public class CommandLocalTime extends CommandPlayerOnlyBase {
    /**
     * 組込み名前付き時間を追加
     * 統合版のほうが充実しているので統合版から拝借してます
     */
    public CommandLocalTime() {
        builtinTimeMap.put("day", 1000);
        builtinTimeMap.put("night", 13000);
        builtinTimeMap.put("noon", 6000);
        builtinTimeMap.put("midnight", 18000);
        builtinTimeMap.put("sunrise", 23000);
        builtinTimeMap.put("sunset", 12000);
    }

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        final var world = player.getWorld();
        final var subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set" -> {
                if (args.length != 2) return false;
                final var timeString = args[1];
                try {
                    final var time = toTime(timeString);
                    world.setTime(time);
                    player.sendMessage(ChatColor.RED + "時刻を " + time + "に設定しました");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "時間指定が異常です");
                }
            }
            case "add" -> {
                if (args.length != 2) return false;
                final var timeString = args[1];
                try {
                    final var time = toTime(timeString);
                    world.setTime(world.getTime() + time);
                    player.sendMessage(ChatColor.RED + "時刻を " + world.getTime() + "に設定しました");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "時間指定が異常です");
                }
            }
            case "query" -> {
                if (args.length != 1) return false;
                player.sendMessage(Long.toString(world.getTime()));
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    /**
     * 対応する文字列から時間の数値に変換する関数
     * @param timeString builtinTimeMapにある対応する文字列
     * @return 時間の数値
     */
    private int toTime(String timeString) {
        if (builtinTimeMap.containsKey(timeString)) {
            return builtinTimeMap.get(timeString);
        }
        return Integer.parseInt(timeString);
    }

    /**
     * 時間の数値とそれに対応するmidnightなどの文字列が格納されている
     */
    private static final HashMap<String, Integer> builtinTimeMap = new HashMap<>();

}

