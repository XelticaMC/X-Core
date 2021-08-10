package work.xeltica.craft.core.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.utils.Ticks;

/**
 * カウントダウンコマンド
 * @author Xeltica
 */
public class CommandCountdown extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        try {
            final var count = Integer.parseInt(args[0]);
            if (count > 60) {
                player.sendMessage("60秒を超えるカウントダウンを作成することはできません。");
            }
            final var members = new HashSet<Player>();
            members.add(player);

            if (args.length >= 2) {
                for (final var name : Arrays.copyOfRange(args, 1, args.length - 1)) {
                    final var p = Bukkit.getPlayer(name);
                    if (p == null) {
                        player.sendMessage("プレイヤー " + name + " が見つかりません。");
                        return true;
                    }
                    members.add(p);
                }
            }

            members.forEach(member -> {
                member.sendTitle(args[0], "", 0, 20, 0);
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1, 0.6f); 
            });
            countDown(count - 1, members);

            return true;
        } catch (NumberFormatException e) {
            player.sendMessage("§c第一引数には数値を指定してください");
            return true;
        }
    }

    private void countDown(int times, Set<Player> members) {
        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            if (times > 0) {
                members.forEach(member -> {
                    member.sendTitle(Integer.toString(times), "", 0, 20, 0);
                    member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1, 0.6f); 
                });
                countDown(times - 1, members);
            } else {
                members.forEach(member -> {
                    member.sendTitle("GO!", "", 0, 20, 0);
                    member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1, 1.2f);
                });
            }
        }, Ticks.from(1));
    }

    private void show(Player player, String text, Sound sound, float pitch) {
    }
}
