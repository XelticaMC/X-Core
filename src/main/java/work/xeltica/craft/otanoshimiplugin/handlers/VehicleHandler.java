package work.xeltica.craft.otanoshimiplugin.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import work.xeltica.craft.otanoshimiplugin.VehicleManager;

public class VehicleHandler implements Listener {
    @EventHandler
    public void onEnter(VehicleEnterEvent e) {
        var entity = e.getEntered();
        if (!(entity instanceof Player)) return;
        VehicleManager.getInstance().unregisterVehicle(e.getVehicle());
    }

    @EventHandler
    public void onExit(VehicleExitEvent e) {
        VehicleManager.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onCreated(VehicleCreateEvent e) {
        VehicleManager.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onDestroyed(VehicleDestroyEvent e) {
        var v = e.getVehicle();
        if (VehicleManager.getInstance().isValidVehicle(v)) {
            e.setCancelled(true);
            e.getVehicle().remove();
        }
        VehicleManager.getInstance().unregisterVehicle(v);
    }
}
