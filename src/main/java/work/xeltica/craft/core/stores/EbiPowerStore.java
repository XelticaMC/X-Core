package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import lombok.Getter;
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
        EbiPowerStore.instance = this;

        // エビパワー保存データを読み込む
        ep = new Config("ep", (conf) -> {
            final var c = conf.getConf();
            this.shopItems = (List<EbiPowerItem>)c.getList(CONFIG_KEY_SHOP_ITEMS, new ArrayList<EbiPowerItem>());
        });
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

    public Result tryBuyItem(Player p, EbiPowerItem item) {
        if (!tryTake(p, item.cost())) {
            return Result.NO_ENOUGH_POWER;
        }

        final var res = p.getInventory().addItem(item.item().clone());
        if (res.size() != 0) {
            // 購入失敗なので返金
            tryGive(p, item.cost());

            return Result.NO_ENOUGH_INVENTORY;
        } else {
            return Result.SUCCESS;
        }
    }

    public int get(Player p) {
        final var vault = VaultPlugin.getInstance();
        return (int)vault.getBalance(p);
    }

    public boolean tryGive(Player p, int amount) {
        final var vault = VaultPlugin.getInstance();
        return vault.tryDepositPlayer(p, amount);
    }

    public boolean tryTake(Player p, int amount) {
        final var vault = VaultPlugin.getInstance();
        return vault.tryWithdrawPlayer(p, amount);
    }

    @Getter
    private static EbiPowerStore instance;
    @Getter
    private List<EbiPowerItem> shopItems = new ArrayList<>();

    private final Config ep;

    private static final String CONFIG_KEY_SHOP_ITEMS = "shopItems";

    public enum Result {
        SUCCESS,
        NO_ENOUGH_POWER,
        NO_ENOUGH_INVENTORY,
    }
}
