package work.xeltica.craft.core.handlers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import work.xeltica.craft.core.modules.HubModule;

/**
 * ロビーにまつわるイベントハンドラーをまとめています。
 * @author Xeltica
 */
public class HubHandler implements Listener {
    @EventHandler
    public void onPlayerHurt(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final var player = (Player)e.getEntity();
        if (playerIsInHub(player)) {
            e.setCancelled(true);
            if (e.getCause() == DamageCause.VOID) {
                final var loc = player.getWorld().getSpawnLocation();
                player.teleportAsync(loc, TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent e) {
        final var player = e.getEntity();
        if (playerIsInHub(player)) {
            player.setFoodLevel(20);
            e.setCancelled(true);
        }
    }

    private boolean playerIsInHub(Entity p) {
        return p.getWorld().getName().equalsIgnoreCase("hub2");
    }
}
