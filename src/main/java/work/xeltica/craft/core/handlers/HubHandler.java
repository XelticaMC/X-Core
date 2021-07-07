package work.xeltica.craft.core.handlers;

import org.bukkit.GameMode;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import work.xeltica.craft.core.stores.HubStore;

public class HubHandler implements Listener {
    @EventHandler
    public void onPlayerHurt(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        var id = store().getHubId();
        if (id == null) return;
        var player = (Player)e.getEntity();
        if (player.getWorld().getUID().equals(id)) {
            e.setCancelled(true);
            if (e.getCause() == DamageCause.VOID) {
                // 落ちた
                var loc = store().getHub().getSpawnLocation();
                player.teleport(loc, TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var world = store().getHub();
        if (store().getForceAll()) {
            player.teleport(world.getSpawnLocation());
        }
        if (!player.hasPlayedBefore() && world != null) {
            var loc = world.getSpawnLocation();
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(loc, TeleportCause.PLUGIN);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var p = e.getPlayer();
        if (playerIsInHub(p)) {
            store().removeSign(p, e.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractBlock(PlayerInteractEvent e) {
        var p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (playerIsInHub(p)) {
            e.setCancelled(store().processSigns(e.getClickedBlock().getLocation(), p));
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        var player = e.getPlayer();
        if (playerIsInHub(player)) {
            e.setCancelled(true);
            store().returnToWorld(player);
        }
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent e) {
        var player = e.getEntity();
        if (playerIsInHub(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        var p = e.getPlayer();
        if (!playerIsInHub(p)) return;

        var lines = e.getLines();

        if (lines[0].equals("[Hub]")) {
            var command = lines[1];
            var arg1 = lines[2];
            var arg2 = lines[3];

            if (command.equalsIgnoreCase("teleport")) {
                e.setLine(0, "[§a§lテレポート§r]");
                e.setLine(1, "");
                e.setLine(2, "§b" + arg2);
                e.setLine(3, "§rクリックorタップ");
            } else if (command.equalsIgnoreCase("return")) {
                e.setLine(0, "§a元の場所に帰る");
                e.setLine(1, "");
                e.setLine(2, "");
                e.setLine(3, "§rクリックorタップ");
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 0.5f);
                p.sendMessage("設置に失敗しました。存在しないコマンドです。");
                return;
            }
            store().placeSign(p, e.getBlock().getLocation(), command, arg1, arg2);
        }
    }

    private boolean playerIsInHub(Entity p) {
        return p.getWorld().getUID().equals(store().getHubId());
    }

    private HubStore store() {
        return HubStore.getInstance();
    }
}
