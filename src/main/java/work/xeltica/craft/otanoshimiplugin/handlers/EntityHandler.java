package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityHandler implements Listener {
    
    public void onEntityDeath(EntityDeathEvent e) {
        // var entity = e.getEntity();
        // if (entity.getWorld().getName().equals("nightmare")) {
        //     if (random.nextInt(100) < 1) {
        //        e.getDrops().add(new ItemStack(Material.))
        //     }
        // }
    }

    private final Random random = new Random();
}
