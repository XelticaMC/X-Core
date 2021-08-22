package work.xeltica.craft.core.models;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Xeltica
 */
public record NbsModel(Location location, String songId, int distance) implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final var result = new HashMap<String, Object>();
        result.put("location", location.serialize());
        result.put("songId", songId);
        result.put("distance", distance);
        return result;
    }

    public static NbsModel deserialize(Map<String, Object> args) {
        final Location location;
        final String songId;
        final int distance;
        if (!args.containsKey("location"))  throw new IllegalArgumentException("location is null");
        if (!args.containsKey("command")) throw new IllegalArgumentException("command is null");
        if (!args.containsKey("distance")) throw new IllegalArgumentException("distance is null");

        location = Location.deserialize((Map<String, Object>) args.get("location"));
        songId = ((String) args.get("songId"));
        distance = ((Integer) args.get("distance"));

        return new NbsModel(location, songId, distance);
    }
}
