package work.xeltica.craft.core.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.modules.PlayerStoreModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ライブモードに関するイベントハンドラをまとめています。
 * @author kumitatepazuru
 */
public class LiveModeHandler implements Listener {
    /**
     * ライブモード中にプレイヤーが退出したときに一定時間後に自動的にライブモードを解除する関数
     * @param e イベント
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (PlayerStoreModule.getliveBarMap().containsKey(uuid)) {
            PlanToDeleteMap.put(uuid, Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                PlayerStoreModule.setLiveMode(p,false);
                PlanToDeleteMap.remove(uuid);
            }, GRACE_TIME_TICK));
        }
    }

    /**
     * 一旦退出した後一定期間以内に再ログインしたときに解除しないようにする関数
     * @param e イベント
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final UUID playerUUID = e.getPlayer().getUniqueId();
        if (PlanToDeleteMap.containsKey(playerUUID)) {
            PlanToDeleteMap.get(playerUUID).cancel();
        }
    }

    private final Map<UUID,BukkitTask> PlanToDeleteMap = new HashMap<>();
    private static final long GRACE_TIME_TICK = 1200L;
}
