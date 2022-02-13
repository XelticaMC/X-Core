package work.xeltica.craft.core.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public record CraftRecipe(ArrayList<ItemStack> ingredients, ItemStack result) {
    public Map<Material, Integer> getFixedRecipe() {
        return ingredients.stream().collect(
                Collectors.groupingBy(ItemStack::getType, Collectors.summingInt(ItemStack::getAmount))
        );
    }
}
