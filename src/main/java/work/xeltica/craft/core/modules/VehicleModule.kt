package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;

import work.xeltica.craft.core.api.Config;

/**
 * サーバーに存在する乗り物を管理し、不要なものはデスポーンする処理などを行うストアです。
 * @author Xeltica
 */
public class VehicleStore {
    public VehicleStore() {
        VehicleStore.instance = this;
        logger = Bukkit.getLogger();
        this.cm = new Config("vehicles");
    }

    public static VehicleStore getInstance() {
        return VehicleStore.instance;
    }

    public void registerVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }
        final var id = vehicle.getUniqueId().toString();

        // 初期値を登録
        cm.getConf().set(id, 20 * 60 * 5);
        try {
            cm.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unregisterVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }

        final var id = vehicle.getUniqueId().toString();

        // 削除
        unregisterVehicle(id);
    }

    // TODO: ワーカーとして書き直す
    public void tick(int tickCount) {
        final var c = this.cm.getConf();
        final var ids = c.getKeys(false);
        for (var id : ids) {
            var val = c.getInt(id);
            val -= tickCount;
            c.set(id, val);
            if (val <= 0) {
                final var e = Bukkit.getEntity(UUID.fromString(id));
                if (e == null) {
                    logger.warning("A vehicle ID:" + id + " is not found on the server, so skipped to despawn.");
                } else {
                    e.remove();
                }
                unregisterVehicle(id);
            }
        }
        try {
            cm.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidVehicle(Vehicle v) {
        return v instanceof Boat || v instanceof RideableMinecart;
    }

    private void unregisterVehicle(String id) {
        // 削除
        cm.getConf().set(id, null);
        try {
            cm.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static VehicleStore instance;
    private final Config cm;
    private final Logger logger;
}
