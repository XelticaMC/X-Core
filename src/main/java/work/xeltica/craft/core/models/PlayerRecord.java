package work.xeltica.craft.core.models;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import work.xeltica.craft.core.utils.Config;

public class PlayerRecord {
    PlayerRecord(Config conf, ConfigurationSection section, UUID playerId) {
        this.conf = conf;
        this.section = section;
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void set(PlayerDataKey key, Object value) {
        set(key, value, true);
    }

    public void set(PlayerDataKey key, Object value, boolean save) {
        if (value != null && value.equals(get(key)))
            return;
        if (get(key) == null && value == null)
            return;
        section.set(key.getPhysicalKey(), value);
        Bukkit.getLogger().info(String.format("%s = %s", key.getPhysicalKey(), value == null ? null : value.toString()));
        if (save) save();
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

    public void delete(PlayerDataKey key) {
        set(key, null);
    }

    public void delete(PlayerDataKey key, boolean save) {
        set(key, null, save);
    }

    private void save() {
        try {
            conf.save();
            Bukkit.getLogger().info("Saved Player Store");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Config conf;
    private final ConfigurationSection section;
    private final UUID playerId;
}