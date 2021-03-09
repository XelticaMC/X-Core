package work.xeltica.craft.otanoshimiplugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.plugin.Plugin;

public class VehicleManager {
    public VehicleManager(Plugin pl) {
        this.pl = pl;
        VehicleManager.instance = this;
        logger = Bukkit.getLogger();
        reloadStore();
    }

    public static VehicleManager getInstance() {
        return VehicleManager.instance;
    }

    public void registerVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }
        var id = vehicle.getUniqueId().toString();

        // 初期値を登録
        conf.set(id, 20 * 60 * 5);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Registered vehicle ID:" + id);
    }

    public void unregisterVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }

        var id = vehicle.getUniqueId().toString();

        // 削除
        unregisterVehicle(id);
        logger.info("Unregistered vehicle ID:" + id);
    }

    public void tick(int tickCount) {
        var ids = conf.getKeys(false);
        for (var id : ids) {
            var val = conf.getInt(id);
            val -= tickCount;
            conf.set(id, val);
            if (val <= 0) {
                var e = Bukkit.getEntity(UUID.fromString(id));
                if (e == null) {
                    logger.warning("A vehicle ID:" + id + " is not found on the server, so skipped to despawn.");
                } else {
                    logger.info("A vehicle ID:" + id + "(" + e.getType() + ") has been destroyed");
                    e.remove();
                }
                unregisterVehicle(id);
            }
        }
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidVehicle(Vehicle v) {
        return v instanceof Boat || v instanceof RideableMinecart;
    }

    public void reloadStore() {
        var confFile = new File(pl.getDataFolder(), "vehicles.yml");
        conf = YamlConfiguration.loadConfiguration(confFile);
    }

    public void writeStore() throws IOException {
        var confFile = new File(pl.getDataFolder(), "vehicles.yml");
        conf.save(confFile);
        conf = YamlConfiguration.loadConfiguration(confFile);
    }

    private void unregisterVehicle(String id) {
        // 削除
        conf.set(id, null);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static VehicleManager instance;
    private Plugin pl;
    private YamlConfiguration conf;
    private Logger logger;
}
