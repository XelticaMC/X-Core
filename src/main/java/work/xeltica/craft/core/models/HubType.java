package work.xeltica.craft.core.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import lombok.Getter;

public enum HubType {
    // メインロビー
    Main("hub2"),
    // メインロビー(新規)
    NewComer("hub2", new Vector3(293, 11, 63)),
    // クラシックロビー
    Classic("hub"),
    ;

    HubType(String worldName) {
        this.worldName = worldName;
    }

    HubType(String worldName, Vector3 vec) {
        this(worldName);
        this.location = vec;
    }

    public int getX() {
        return location.x();
    }

    public int getY() {
        return location.y();
    }

    public int getZ() {
        return location.z();
    }

    public Location getSpigotLocation() {
        return new Location(Bukkit.getWorld(getWorldName()), getX() + .5, getY() + .5, getZ() + .5);
    }

    @Getter
    String worldName;
    @Getter
    Vector3 location;
}
