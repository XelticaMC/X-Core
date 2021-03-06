package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import work.xeltica.craft.otanoshimiplugin.VehicleManager;

public class VehicleHandler implements Listener {
    public void onEnter(VehicleEnterEvent e) {
        var entity = e.getEntered();
        if (!(entity instanceof Player)) return;
        VehicleManager.getInstance().unregisterVehicle(e.getVehicle());
    }

    public void onExit(VehicleExitEvent e) {
        VehicleManager.getInstance().registerVehicle(e.getVehicle());
    }

    public void onCreated(VehicleCreateEvent e) {
        VehicleManager.getInstance().registerVehicle(e.getVehicle());
    }

    public void onDestroyed(VehicleDestroyEvent e) {
        VehicleManager.getInstance().unregisterVehicle(e.getVehicle());
    }
}
