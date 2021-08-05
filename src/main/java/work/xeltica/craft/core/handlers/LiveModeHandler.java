package work.xeltica.craft.core.handlers;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.stores.BossBarStore;
import work.xeltica.craft.core.stores.PlayerStore;

import java.util.HashMap;
import java.util.UUID;
// TODO: javadoc書く！&logger消す
public class LiveModeHandler implements Listener {
    HashMap<UUID,BukkitTask> PlanToDeleteMap = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        final UUID playerUUID = (e.getPlayer()).getUniqueId();
        final PlayerStore instance = PlayerStore.getInstance();
        if (instance.liveBarMap.containsKey(playerUUID)) {
            PlanToDeleteMap.put(playerUUID, Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                final BossBar bar = instance.liveBarMap.get(playerUUID);
                instance.liveBarMap.remove(playerUUID);
                BossBarStore.getInstance().remove(bar);
                PlanToDeleteMap.remove(playerUUID);
                Bukkit.getLogger().info("removed");
            }, 200L)); //TODO: Debug用時間を1200に修正する
            Bukkit.getLogger().info("remove livemode after 20tick");
        } else {
            Bukkit.getLogger().info("already disabled livemode");
        }
    }
}
