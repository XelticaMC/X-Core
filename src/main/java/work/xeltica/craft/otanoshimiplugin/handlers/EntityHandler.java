package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityHandler implements Listener {
    
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.MINECART || e.getEntityType() == EntityType.BOAT) {
            e.getDrops().clear();
        }
    }

    private final Random random = new Random();
}
