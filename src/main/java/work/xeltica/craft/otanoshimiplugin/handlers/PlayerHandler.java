package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.otanoshimiplugin.OmikujiScore;
import work.xeltica.craft.otanoshimiplugin.OmikujiStore;
import work.xeltica.craft.otanoshimiplugin.utils.TravelTicketUtil;

public class PlayerHandler implements Listener {
    public PlayerHandler(Plugin p) {
        this.plugin = p;
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        var p = (Player)e.getEntity();
        if (p.getHealth() - e.getFinalDamage() > 0)
            return;

        var score = OmikujiStore.getInstance().get(p);
        var th = 
            score == OmikujiScore.Tokudaikichi ? 5 : 
            score == OmikujiScore.Daikichi ? 1 : 0;
        
        if ((int)(Math.random() * 100) >= th) return;

        var i = p.getInventory();
        var heldItem = i.getItemInMainHand();
        i.remove(heldItem);
        i.setItemInMainHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        new BukkitRunnable(){
            @Override
            public void run() {
                i.setItemInMainHand(heldItem);
            }
        }.runTaskLater(this.plugin, 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        var name = p.getDisplayName();
        e.setJoinMessage(ChatColor.GREEN + name + ChatColor.AQUA + "がやってきました");
        if (!p.hasPlayedBefore()) {
            e.setJoinMessage(ChatColor.GREEN + name + ChatColor.AQUA + "が" + ChatColor.GOLD + ChatColor.BOLD + "初参加" + ChatColor.RESET + "です");
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        // サンドボックスと旅行先からポータルを開けることを禁止

        var name = e.getPlayer().getWorld().getName();
        if (name.startsWith("travel_") || name.equals("sandbox")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        var name = e.getPlayer().getDisplayName();
        e.setQuitMessage(ChatColor.GREEN + name + ChatColor.AQUA + "がかえりました");
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        var isEC = e.getInventory().getType() == InventoryType.ENDER_CHEST;
        var isSB = e.getPlayer().getWorld().getName().equals("sandbox");
        if (isEC && isSB) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent e) {
        var p = e.getPlayer();
        if (p.getWorld().getName().startsWith("travel_")) {
            var world = Bukkit.getWorld("world");
            p.teleportAsync(world.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        var p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var worldName = p.getWorld().getName();
            var isBedDisabledWorld = 
                   worldName.startsWith("travel_")
                || worldName.equals("hub")
                || worldName.equals("sandbox")
                ;
            if (isBedDisabledWorld && Tag.BEDS.isTagged(e.getClickedBlock().getType())) {
                e.setCancelled(true);
            }
            return;
        }
        var item = e.getItem();
        if (TravelTicketUtil.isTravelTicket(item) && e.getAction() != Action.PHYSICAL) {
            e.setCancelled(true);

            if (movingPlayer != null) {
                p.sendMessage(p.getUniqueId().equals(movingPlayer) ? "移動中です！" : "誰かが移動中です。少し待ってから再試行してください。");
                return;
            }

            var type = TravelTicketUtil.getTicketType(item);
            var worldName = "travel_" + type.toString().toLowerCase();
            var world = Bukkit.getWorld(worldName);
            
            if (world == null) {
                p.sendMessage("申し訳ありませんが、現在" + type.getDisplayName() + "には行けません。");
                return;
            }
            if (world.getUID().equals(p.getWorld().getUID())) {
                p.sendMessage("既に来ています！");
                return;
            }
            movingPlayer = p.getUniqueId();

            if (p.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
            int x, z;
            var otherPlayers = world.getPlayers();
            if (otherPlayers.size() == 0) {
                x = rnd.nextInt(20000) - 10000;
                z = rnd.nextInt(20000) - 10000;
            } else {
                var chosen = otherPlayers.get(rnd.nextInt(otherPlayers.size()));
                var loc = chosen.getLocation();
                final var range = 10;
                x = loc.getBlockX() + rnd.nextInt(range) - range / 2;
                z = loc.getBlockZ() + rnd.nextInt(range) - range / 2;
            }
            p.sendMessage("旅行券を使用しました。現在手配中です。その場で少しお待ちください！");
            var res = world.getChunkAtAsync(x, z);
            res.whenComplete((ret, ex) -> {
                var y = world.getHighestBlockYAt(x, z);
                var loc = new Location(world, x, y, z);
                var block = world.getBlockAt(loc);
                // 危険ブロックの場合、安全な石ブロックを敷いておく
                // TODO: 対象ブロックをHashSetに入れてそれを使うようにする
                if (block.getType() == Material.LAVA || block.getType() == Material.WATER) {
                    block.setType(Material.STONE, false);
                }
                for (var pl : Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(String.format("§6%s§rさんが§a%s§rに行きます！§b行ってらっしゃい！", p.getDisplayName(), type.getDisplayName()));
                }
                p.teleport(loc);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1, 0.5f);
                p.sendTitle(ChatColor.GOLD + type.getDisplayName(), "良い旅を！", 5, 100, 5);
                p.sendMessage("ようこそ、§6" + type.getDisplayName() + "§rへ！");
                p.sendMessage("元の世界に帰る場合は、§a/respawn§rコマンドを使用します。");
                movingPlayer = null;
            });
        }
    }

    private Plugin plugin;
    private final Random rnd = new Random();
    private UUID movingPlayer = null;
}
