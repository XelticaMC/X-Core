package work.xeltica.craft.core.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * エビパワーストアで取り扱う商品を表すモデルです。
 * @author Xeltica
 */
public record EbiPowerItem(ItemStack item, int cost) implements Cloneable, ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final var result = new LinkedHashMap<String, Object>();
        result.put("item", item.serialize());
        result.put("cost", cost);

        return result;
    }

    public static EbiPowerItem deserialize(Map<String, Object> args) {
        final var item = ItemStack.deserialize((Map<String, Object>) args.get("item"));
        final var cost = (int) args.get("cost");

        return new EbiPowerItem(item, cost);
    }
}
