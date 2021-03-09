package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityHandler implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.MINECART || e.getEntityType() == EntityType.BOAT) {
            Bukkit.getLogger().info("Remove " + e.getEntityType());
            e.getDrops().clear();
        }
    }
}
