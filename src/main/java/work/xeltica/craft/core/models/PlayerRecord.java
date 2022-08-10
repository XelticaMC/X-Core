package work.xeltica.craft.core.models;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import work.xeltica.craft.core.api.Config;
import work.xeltica.craft.core.stores.PlayerStore;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * プレイヤーごとのデータを読み書きするインターフェイスです。
 * @author Xeltica
 */
public class PlayerRecord {
    public PlayerRecord(Config conf, ConfigurationSection section, UUID playerId) {
        this.conf = conf;
        this.section = section;
        this.playerId = playerId;
    }

    public void set(PlayerDataKey key, Object value) {
        if (value != null && value.equals(get(key))) return;
        if (get(key) == null && value == null) return;
        section.set(key.getPhysicalKey(), value);
        PlayerStore.getInstance().setChanged(true);
    }

    @Deprecated(since = "Use set(PlayerDataKey, Object) instead.")
    public void set(PlayerDataKey key, Object value, boolean save) {
        set(key, value);
    }

    public boolean has(PlayerDataKey key) {
        return section.contains(key.getPhysicalKey());
    }

    public Object get(PlayerDataKey key) {
        return section.get(key.getPhysicalKey());
    }

    public Object get(PlayerDataKey key, Object defaultValue) {
        return section.get(key.getPhysicalKey(), defaultValue);
    }

    public String getString(PlayerDataKey key) {
        return section.getString(key.getPhysicalKey());
    }

    public String getString(PlayerDataKey key, String defaultValue) {
        return section.getString(key.getPhysicalKey(), defaultValue);
    }

    public boolean isString(PlayerDataKey key) {
        return section.isString(key.getPhysicalKey());
    }

    public List<String> getStringList(PlayerDataKey key) {
        return section.getStringList(key.getPhysicalKey());
    }

    public boolean isList(PlayerDataKey key) {
        return section.isList(key.getPhysicalKey());
    }

    public int getInt(PlayerDataKey key) {
        return section.getInt(key.getPhysicalKey());
    }

    public int getInt(PlayerDataKey key, int defaultValue) {
        return section.getInt(key.getPhysicalKey(), defaultValue);
    }

    public boolean isInt(PlayerDataKey key) {
        return section.isInt(key.getPhysicalKey());
    }

    public boolean getBoolean(PlayerDataKey key) {
        return section.getBoolean(key.getPhysicalKey());
    }

    public boolean getBoolean(PlayerDataKey key, boolean defaultValue) {
        return section.getBoolean(key.getPhysicalKey(), defaultValue);
    }

    public boolean isBoolean(PlayerDataKey key) {
        return section.isBoolean(key.getPhysicalKey());
    }

    public double getDouble(PlayerDataKey key) {
        return section.getDouble(key.getPhysicalKey());
    }

    public double getDouble(PlayerDataKey key, double defaultValue) {
        return section.getDouble(key.getPhysicalKey(), defaultValue);
    }

    public boolean isDouble(PlayerDataKey key) {
        return section.isDouble(key.getPhysicalKey());
    }

    public long getLong(PlayerDataKey key) {
        return section.getLong(key.getPhysicalKey());
    }

    public long getLong(PlayerDataKey key, long defaultValue) {
        return section.getLong(key.getPhysicalKey(), defaultValue);
    }

    public boolean isLong(PlayerDataKey key) {
        return section.isLong(key.getPhysicalKey());
    }

    public Vector getVector(PlayerDataKey key) {
        return section.getVector(key.getPhysicalKey());
    }

    public Vector getVector(PlayerDataKey key, Vector defaultValue) {
        return section.getVector(key.getPhysicalKey(), defaultValue);
    }

    public boolean isVector(PlayerDataKey key) {
        return section.isVector(key.getPhysicalKey());
    }

    public Location getLocation(PlayerDataKey key) {
        return section.getLocation(key.getPhysicalKey());
    }

    public Location getLocation(PlayerDataKey key, Location defaultValue) {
        return section.getLocation(key.getPhysicalKey(), defaultValue);
    }

    public boolean isLocation(PlayerDataKey key) {
        return section.isLocation(key.getPhysicalKey());
    }

    public void delete(PlayerDataKey key) {
        set(key, null);
    }

    @Deprecated(since = "Use set(PlayerDataKey, Object) instead.")
    public void delete(PlayerDataKey key, boolean save) {
        set(key, null, save);
    }

    private void save() {
        try {
            conf.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Config conf;
    private final ConfigurationSection section;

    private final UUID playerId;

    public UUID getPlayerId() {
        return this.playerId;
    }
}
