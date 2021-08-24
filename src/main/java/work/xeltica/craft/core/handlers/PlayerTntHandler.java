package work.xeltica.craft.core.handlers;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTntHandler implements Listener {
    @EventHandler
    public void onTNTPrime(TNTPrimeEvent e){
        if (e.getBlock().isBlockPowered()) {
            e.setCancelled(true);
        }
    }
}
