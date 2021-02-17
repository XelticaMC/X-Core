package work.xeltica.craft.otanoshimiplugin.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.otanoshimiplugin.OmikujiScore;
import work.xeltica.craft.otanoshimiplugin.OmikujiStore;

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
    public void onPlayerQuit(PlayerQuitEvent e) {
        var name = e.getPlayer().getDisplayName();
        e.setQuitMessage(ChatColor.GREEN + name + ChatColor.AQUA + "がかえりました");
    }

    private Plugin plugin;
}
