package work.xeltica.craft.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Xeltica
 */
@Data
@AllArgsConstructor
public class NbsModel implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final var result = new HashMap<String, Object>();
        result.put("location", location.serialize());
        result.put("songId", songId);
        result.put("distance", distance);
        result.put("playbackMode", playbackMode.toString());
        return result;
    }

    public static NbsModel deserialize(Map<String, Object> args) {
        final Location location;
        final String songId;
        final int distance;
        final PlaybackMode playbackMode;

        if (!args.containsKey("location")) throw new IllegalArgumentException("location is null");
        if (!args.containsKey("songId")) throw new IllegalArgumentException("songId is null");
        if (!args.containsKey("distance")) throw new IllegalArgumentException("distance is null");

        location = Location.deserialize((Map<String, Object>) args.get("location"));
        songId = ((String) args.get("songId"));
        distance = ((Integer) args.get("distance"));
        if (!args.containsKey("playbackMode")) {
            playbackMode = PlaybackMode.NORMAL;
        } else {
            playbackMode = PlaybackMode.valueOf((String) args.get("playbackMode"));
        }

        return new NbsModel(location, songId, distance, playbackMode);
    }

    public enum PlaybackMode {
        /** 普通 */
        NORMAL,
        /** ループする */
        LOOP,
        /** トグルで停止せずに鳴り続ける */
        ONESHOT
    }

    Location location;
    String songId;
    int distance;
    PlaybackMode playbackMode;
}
