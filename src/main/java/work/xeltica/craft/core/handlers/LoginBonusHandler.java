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
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule;
import work.xeltica.craft.core.modules.player.PlayerDataKey;
import work.xeltica.craft.core.modules.player.PlayerModule;
import work.xeltica.craft.core.utils.Ticks;

public class LoginBonusHandler implements Listener {
    @EventHandler
    public void onLoginBonus(RealTimeNewDayEvent e) {
        final var playerModule = PlayerModule.INSTANCE;
        final var records = playerModule.openAll();

        // ログボ記録を削除
        records.forEach(record -> record.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS));
        records.forEach(record -> record.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER));

        // いる人にログボ
        Bukkit.getOnlinePlayers().forEach(this::giveLoginBonus);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        giveLoginBonus(e.getPlayer());
    }

    private void giveLoginBonus(Player p) {
        final var playerModule = PlayerModule.INSTANCE;
        final var record = playerModule.open(p);

        if (!record.getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS)) {
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                if (!p.isOnline()) return;
                EbiPowerModule.INSTANCE.tryGive(p, LOGIN_BONUS_EBIPOWER);
                p.sendMessage("§a§lログインボーナス達成！§6" + LOGIN_BONUS_EBIPOWER + "EP§fを手に入れた！");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
                playerModule.open(p).set(PlayerDataKey.RECEIVED_LOGIN_BONUS, true);
            }, Ticks.from(2));
        }
    }

    private static final int LOGIN_BONUS_EBIPOWER = 250;
}
