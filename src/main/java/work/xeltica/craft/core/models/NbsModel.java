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
public class NbsModel implements Cloneable, ConfigurationSerializable {

    public NbsModel(Location location, String songId, int distance, PlaybackMode playbackMode) {
        this.location = location;
        this.songId = songId;
        this.distance = distance;
        this.playbackMode = playbackMode;
    }

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

    public Location getLocation() {
        return this.location;
    }

    public String getSongId() {
        return this.songId;
    }

    public int getDistance() {
        return this.distance;
    }

    public PlaybackMode getPlaybackMode() {
        return this.playbackMode;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setPlaybackMode(PlaybackMode playbackMode) {
        this.playbackMode = playbackMode;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NbsModel)) return false;
        final NbsModel other = (NbsModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        final Object this$songId = this.getSongId();
        final Object other$songId = other.getSongId();
        if (this$songId == null ? other$songId != null : !this$songId.equals(other$songId)) return false;
        if (this.getDistance() != other.getDistance()) return false;
        final Object this$playbackMode = this.getPlaybackMode();
        final Object other$playbackMode = other.getPlaybackMode();
        if (this$playbackMode == null ? other$playbackMode != null : !this$playbackMode.equals(other$playbackMode))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NbsModel;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $songId = this.getSongId();
        result = result * PRIME + ($songId == null ? 43 : $songId.hashCode());
        result = result * PRIME + this.getDistance();
        final Object $playbackMode = this.getPlaybackMode();
        result = result * PRIME + ($playbackMode == null ? 43 : $playbackMode.hashCode());
        return result;
    }

    public String toString() {
        return "NbsModel(location=" + this.getLocation() + ", songId=" + this.getSongId() + ", distance=" + this.getDistance() + ", playbackMode=" + this.getPlaybackMode() + ")";
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
