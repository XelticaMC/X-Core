package work.xeltica.craft.core.handlers;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.modules.hub.HubModule;
import work.xeltica.craft.core.stores.WorldStore;

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

    private HubModule store() {
        return HubModule.INSTANCE;
    }
}
