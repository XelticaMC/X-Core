package work.xeltica.craft.core.models;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import work.xeltica.craft.core.api.Ticks;

/**
 * エビパワードラッグストアの商品を表します。
 */
public record EbiPowerEffect(PotionEffectType effectType, int level, int time, int cost)
        implements Cloneable, ConfigurationSerializable {
    @Override
    public Map<String, Object> serialize() {
        final var result = new HashMap<String, Object>();
        result.put("effectType", effectType.getName());
        result.put("level", level - 1);
        result.put("time", time);
        result.put("cost", cost);

        return result;
    }

    public PotionEffect toPotionEffect() {
        return new PotionEffect(effectType, Ticks.from(time), level);
    }


    public static EbiPowerEffect deserialize(Map<String, Object> args) {
        final var effectType = PotionEffectType.getByName((args.get("effectType").toString()));

        final var level = (int) args.get("level");
        final var time = (int) args.get("time");
        final var cost = (int) args.get("cost");

        if (effectType == null) throw new IllegalArgumentException();

        return new EbiPowerEffect(effectType, level, time, cost);
    }
}
