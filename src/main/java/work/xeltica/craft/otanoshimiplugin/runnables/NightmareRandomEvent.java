package work.xeltica.craft.otanoshimiplugin.runnables;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
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

        for (var player : players) {
            var l = player.getLocation();

            var dice = random.nextInt(5);
            final var MODE_PHANTOM = 0;
            final var MODE_PIGLIN = 1;

            if (dice == MODE_PHANTOM) {
                var phantoms = nightmare.getEntitiesByClass(Phantom.class).size();
                if (phantoms < players.size()) {
                    var spawnLoc = l
                            .add(new Vector(random.nextInt(16) - 8, random.nextInt(4) + 6, random.nextInt(16) - 8));
                    var phantom = (Phantom) nightmare.spawnEntity(spawnLoc, EntityType.PHANTOM);
                    var ratio = random.nextInt(100);
                    // 1%の確率でスケルトンが騎乗
                    if (ratio == 10) {
                        var skeleton = (Skeleton) nightmare.spawnEntity(spawnLoc, EntityType.SKELETON);
                        phantom.addPassenger(skeleton);
                    }
                }
            } else if (dice == MODE_PIGLIN) {
                // 1~3体出す
                var amount = random.nextInt(3) + 1;
                var pivot = l.add(new Vector(random.nextInt(20) + 10, 0, random.nextInt(20) + 10));
                for (var i = 0; i < amount; i++) {
                    var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                    var piglin = (Piglin) nightmare.spawnEntity(loc, EntityType.PIGLIN);
                    // 50:50で武器が決まる
                    var equip = new ItemStack(
                        random.nextBoolean()
                            ? Material.CROSSBOW
                            : Material.GOLDEN_SWORD
                    );
                    // 90%の確率でゾンビ化を免れる
                    piglin.setImmuneToZombification(random.nextInt(100) < 90);
                    piglin.getEquipment().setItemInMainHand(equip);
                    // 5%の確率でスピードバフ
                    if (random.nextInt(100) < 5) {
                        piglin.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30000, 2));
                    }
                    // 5%の確率で子供
                    if (random.nextInt(100) < 5) {
                        piglin.setBaby();
                    }
                }
            }
        }
    }

    private Plugin plugin;
    private final Random random = new Random();
}
