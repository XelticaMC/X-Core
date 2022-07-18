package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import work.xeltica.craft.core.models.EbiPowerEffect;
import work.xeltica.craft.core.models.EbiPowerItem;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.utils.Config;

/**
 * エビパワーショップの販売品管理などを行います。
 * @author Xeltica
 */
public class EbiPowerStore {
    public EbiPowerStore() {
        ConfigurationSerialization.registerClass(EbiPowerItem.class, "EbiPowerItem");
        ConfigurationSerialization.registerClass(EbiPowerEffect.class, "EbiPowerEffect");
        EbiPowerStore.instance = this;

        // エビパワー保存データを読み込む
        ep = new Config("ep", (conf) -> {
            final var c = conf.getConf();
            this.shopItems = (List<EbiPowerItem>)c.getList(CONFIG_KEY_SHOP_ITEMS, new ArrayList<EbiPowerItem>());
            this.effectShopItems = (List<EbiPowerEffect>)c.getList(
                    CONFIG_KEY_EFFECT_SHOP_ITEMS, new ArrayList<EbiPowerEffect>());
        });
    }

    public static EbiPowerStore getInstance() {
        return EbiPowerStore.instance;
    }

    public void deleteItem(EbiPowerItem item) {
        shopItems.remove(item);
        ep.getConf().set(CONFIG_KEY_SHOP_ITEMS, shopItems);
        try {
            ep.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(EbiPowerItem item) {
        shopItems.add(item);
        ep.getConf().set(CONFIG_KEY_SHOP_ITEMS, shopItems);
        try {
            ep.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(EbiPowerEffect item) {
        effectShopItems.remove(item);
        ep.getConf().set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems);
        try {
            ep.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(EbiPowerEffect item) {
        effectShopItems.add(item);
        ep.getConf().set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems);
        try {
            ep.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Result tryBuyItem(Player p, EbiPowerItem item) {
        final var isFree = item.cost() == 0;
        if (!isFree && !tryTake(p, item.cost())) {
            return Result.NO_ENOUGH_POWER;
        }

        final var res = p.getInventory().addItem(item.item().clone());
        if (res.size() != 0) {
            // 購入失敗なので返金
            if (!isFree) tryGive(p, item.cost());
            final var partiallyAddedItemsCount = item.item().getAmount() - res.get(0).getAmount();
            if (partiallyAddedItemsCount > 0) {
                // 部分的に追加されてしまったアイテムを剥奪
                p.getInventory().remove(new ItemStack(item.item().getType(), partiallyAddedItemsCount));
            }

            return Result.NO_ENOUGH_INVENTORY;
        } else {
            return Result.SUCCESS;
        }
    }

    public Result tryBuyItem(Player p, EbiPowerEffect item) {
        final var isFree = item.cost() == 0;
        if (!isFree && !tryTake(p, item.cost())) {
            return Result.NO_ENOUGH_POWER;
        }

        p.addPotionEffect(item.toPotionEffect());
        return Result.SUCCESS;
    }

    public int get(Player p) {
        final var vault = VaultPlugin.getInstance();
        return (int)vault.getBalance(p);
    }

    public boolean tryGive(Player p, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amountを0以下の数にはできない");
        final var vault = VaultPlugin.getInstance();
        return vault.tryDepositPlayer(p, amount);
    }

    public boolean tryTake(Player p, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amountを0以下の数にはできない");
        final var vault = VaultPlugin.getInstance();
        return vault.tryWithdrawPlayer(p, amount);
    }

    private static EbiPowerStore instance;
    private List<EbiPowerItem> shopItems = new ArrayList<>();

    private List<EbiPowerEffect> effectShopItems = new ArrayList<>();

    private final Config ep;

    private static final String CONFIG_KEY_SHOP_ITEMS = "shopItems";
    private static final String CONFIG_KEY_EFFECT_SHOP_ITEMS = "effectShopItems";

    public List<EbiPowerItem> getShopItems() {
        return this.shopItems;
    }

    public List<EbiPowerEffect> getEffectShopItems() {
        return this.effectShopItems;
    }

    public enum Result {
        SUCCESS,
        NO_ENOUGH_POWER,
        NO_ENOUGH_INVENTORY,
    }
}
