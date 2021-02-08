package work.xeltica.craft.otanoshimiplugin.runnables;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NightmareRandomEvent extends BukkitRunnable {
    public NightmareRandomEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        var nightmare = plugin.getServer().getWorld("nightmare");
        var players = nightmare.getPlayers();
        if (players.size() == 0) return;
        var player = players.get(random.nextInt(players.size()));
        var l = player.getLocation();

        // ファントム
        var phantoms = nightmare.getEntitiesByClass(Phantom.class).size();
        if (phantoms < players.size()) {
            var spawnLoc = l.add(new Vector(random.nextInt(16) - 8, random.nextInt(4) + 6, random.nextInt(16) - 8));  
            var phantom = (Phantom)nightmare.spawnEntity(spawnLoc, EntityType.PHANTOM);    
            var ratio = random.nextInt(100);
            plugin.getLogger().info("Skeleton Ratio: " + ratio);
            // 1%の確率でスケルトンが騎乗
            if (ratio != 79) {
                var skeleton = (Skeleton)nightmare.spawnEntity(spawnLoc, EntityType.SKELETON);
                phantom.addPassenger(skeleton);
                phantom.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20 * 60, 3));
            }
        }
    }

    private Plugin plugin;
    private final Random random = new Random();
}
