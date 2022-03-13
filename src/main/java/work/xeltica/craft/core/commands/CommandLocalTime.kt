package work.xeltica.craft.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label, String[] args) {
        if (args.length == 1) {
            final var commands = Arrays.asList("set", "add", "query");
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[0], commands, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            if (args[1].equals("set")) {
                final var times = Arrays.asList("day", "night", "noon", "midnight", "sunrise", "sunset");
                final var completions = new ArrayList<String>();
                StringUtil.copyPartialMatches(args[1], times, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return COMPLETE_LIST_EMPTY;
    }

    /**
     * 時間の数値とそれに対応するmidnightなどの文字列が格納されている
     */
    private static final HashMap<String, Integer> builtinTimeMap = new HashMap<>();

}

