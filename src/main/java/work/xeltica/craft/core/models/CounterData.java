package work.xeltica.craft.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

/**
 * カウンターの位置情報を登録するシステムです。
 */
public class CounterData implements Cloneable, ConfigurationSerializable {

    public CounterData(String name, Location location1, Location location2, boolean isDaily, @Nullable UUID rankingId) {
        this.name = name;
        this.location1 = location1;
        this.location2 = location2;
        this.isDaily = isDaily;
        this.rankingId = rankingId;
    }

    public @NotNull Map<String, Object> serialize() {
        final var serialized = new HashMap<String, Object>();
        serialized.put("name", name);
        serialized.put("location1", location1.serialize());
        serialized.put("location2", location2.serialize());
        serialized.put("isDaily", isDaily);
        if (rankingId != null) {
            serialized.put("rankingId", rankingId.toString());
        }
        return serialized;
    }

    public static CounterData deserialize(Map<String, Object> args) {
        final String name;
        final Location location1;
        final Location location2;
        final boolean isDaily;
        final UUID rankingId;

        assertKey(args, "name");
        assertKey(args, "location1");
        assertKey(args, "location2");
        assertKey(args, "isDaily");

        name = (String)args.get("name");
        location1 = Location.deserialize((Map<String, Object>) args.get("location1"));
        location2 = Location.deserialize((Map<String, Object>) args.get("location2"));
        isDaily = (Boolean)args.get("isDaily");

        rankingId = args.containsKey("rankingId")
            ? UUID.fromString((String)args.get("rankingId"))
            : null;

        return new CounterData(name, location1, location2, isDaily, rankingId);
    }

    protected static void assertKey(Map<String, Object> args, @NotNull String key) {
        if (!args.containsKey(key)) throw new IllegalArgumentException(key + " is null");
    }

    @Getter private final String name;
    @Getter private final Location location1;
    @Getter private final Location location2;
    @Getter private final boolean isDaily;
    @Nullable @Getter private final UUID rankingId;
}
