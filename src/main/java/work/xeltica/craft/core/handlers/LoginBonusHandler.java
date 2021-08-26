package work.xeltica.craft.core.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.events.RealTimeNewDayEvent;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.utils.Ticks;

import java.io.IOException;

public class LoginBonusHandler implements Listener {
    @EventHandler
    public void onLoginBonus(RealTimeNewDayEvent e) {
        final var pstore = PlayerStore.getInstance();
        final var records = pstore.openAll();

        // ログボ記録を削除
        records.forEach(record -> record.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS, false));
        records.forEach(record -> record.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER, false));

        // いる人にログボ
        Bukkit.getOnlinePlayers().forEach(this::giveLoginBonus);
        try {
            pstore.save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        giveLoginBonus(e.getPlayer());
        try {
            PlayerStore.getInstance().save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void giveLoginBonus(Player p) {
        final var pstore = PlayerStore.getInstance();
        final var record = pstore.open(p);

        if (!record.getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS)) {
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                if (!p.isOnline()) return;
                EbiPowerStore.getInstance().tryGive(p, LOGIN_BONUS_EBIPOWER);
                p.sendMessage("§a§lログインボーナス達成！§6" + LOGIN_BONUS_EBIPOWER + "EP§fを手に入れた！");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
                pstore.open(p).set(PlayerDataKey.RECEIVED_LOGIN_BONUS, true);
            }, Ticks.from(2));
        }

        // 夏祭り限定
        if (!record.getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER)) {
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                if (!p.isOnline()) return;
                p.sendMessage("§c§l夏祭り限定ログインボーナス達成！§e花火§fを手に入れた！");
                p.sendMessage("X Phoneで無料で受け取ることができます。");
            }, Ticks.from(5));
        }
    }

    private static final int LOGIN_BONUS_EBIPOWER = 250;
}
