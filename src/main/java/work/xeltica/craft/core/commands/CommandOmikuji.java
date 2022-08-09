package work.xeltica.craft.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.api.Ticks;

import java.util.List;

/**
 * おみくじを引くコマンド
 * @author Xeltica
 */
public class CommandOmikuji extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var s = OmikujiStore.getInstance();

        if (s.isDrawnBy(player)) {
            final var score = s.getScoreName(player);
            player.sendMessage(
                ChatColor.RED + "既に引いています！" +
                ChatColor.GOLD + "あなたの運勢は" +
                ChatColor.RESET + "「" +
                ChatColor.GREEN + score +
                ChatColor.RESET + "」" +
                ChatColor.AQUA + "です！"
            );
            player.sendMessage(ChatColor.GOLD + "また次の朝、引いてください！");
            return true;
        }
        final var vault = VaultPlugin.getInstance();
        if (vault.isEconomyEnabled() && !vault.tryWithdrawPlayer(player, 100)) {
            player.sendMessage(ChatColor.RED + "パワーが足りません！おみくじは1回100エビパワーが必要です。");
            return true;
        }
        player.sendMessage("何が出るかな...?");

        new BukkitRunnable(){
            @Override
            public void run() {
                final var score = s.generateScore();
                s.set(player, score);

                player.sendMessage(
                    ChatColor.GOLD + "あなたの運勢は" +
                    ChatColor.RESET + "「" +
                    ChatColor.GREEN + score.getDisplayName() +
                    ChatColor.RESET + "」" +
                    ChatColor.AQUA + "です！"
                );

                switch (score) {
                    case Daikichi -> {
                        // 大吉。幸運が20分つく
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Ticks.from(20, 0), 1));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 0.7f);
                    }
                    case Daikyou -> {
                        // 大凶。不吉な予感10分、毒10秒、不運が20分つく
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, Ticks.from(10, 0), 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Ticks.from(10), 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0), 1));
                        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1, 0.5f);
                        HintStore.getInstance().achieve(player, Hint.OMIKUJI_DAIKYOU);
                    }
                    case Kyou -> {
                        // 凶。不運が20分、吐き気が5秒つく
                        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0), 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Ticks.from(5), 1));
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1, 0.5f);
                    }
                    case Tokudaikichi -> {
                        // 特大吉。幸運が20分つく
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60 * 20 * 20, 1));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1, 1.4f);
                        HintStore.getInstance().achieve(player, Hint.OMIKUJI_TOKUDAIKICHI);
                    }
                    default ->
                            // その他。特に何もなし
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1.6f);
                }
            }
        }.runTaskLater(XCorePlugin.getInstance(), 20 * 3);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        return COMPLETE_LIST_EMPTY;
    }
}
