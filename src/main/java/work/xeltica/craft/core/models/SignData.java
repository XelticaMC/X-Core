package work.xeltica.craft.core.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import lombok.Getter;

/**
 * クラシックロビーのコマンド看板のデータを表すモデルです。
 * @author Xeltica
 */
public class SignData implements Cloneable, ConfigurationSerializable {
    public SignData(Location location, String command) {
        this(location, command, null);
    }

    public SignData(Location location, String command, String arg1) {
        this(location, command, arg1, null);
    }

    public SignData(Location location, String command, String arg1, String arg2) {
        this.location = location;
        this.command = command;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Map<String, Object> serialize() {
        final var result = new LinkedHashMap<String, Object>();
        result.put("location", location.serialize());
        result.put("command", command);
        result.put("arg1", arg1);
        result.put("arg2", arg2);
        return result;
    }

    public static SignData deserialize(Map<String, Object> args) {
        final Location location;
        final String command;
        final String arg1;
        final String arg2;
        if (!args.containsKey("location"))  throw new IllegalArgumentException("location is null");
        if(!args.containsKey("command")) throw new IllegalArgumentException("command is null");
        if (!args.containsKey("arg1")) throw new IllegalArgumentException("arg1 is null");
        if (!args.containsKey("arg2")) throw new IllegalArgumentException("arg2 is null");

        location = Location.deserialize((Map<String, Object>) args.get("location"));
        command = ((String) args.get("command"));
        arg1 = ((String) args.get("arg1"));
        arg2 = ((String) args.get("arg2"));

        return new SignData(location, command, arg1, arg2);
    }

    @Getter
    private final Location location;
    @Getter
    private final String command;
    @Getter
    private final String arg1;
    @Getter
    private final String arg2;
}
