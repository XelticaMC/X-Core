package work.xeltica.craft.core.handlers;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PlayerTntHandler implements Listener {
    @EventHandler
    public void onTNTPrime(TNTPrimeEvent e){
        final var isBlacklistedReason = List.of(TNTPrimeEvent.PrimeReason.REDSTONE, TNTPrimeEvent.PrimeReason.PROJECTILE).contains(e.getReason());
        final var isInMain = "main".equals(e.getBlock().getWorld().getName());
        if (isInMain && isBlacklistedReason) {
            e.setCancelled(true);
        }
    }
}
