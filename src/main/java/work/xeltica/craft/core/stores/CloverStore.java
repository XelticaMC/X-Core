package work.xeltica.craft.core.stores;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.api.Ticks;
import work.xeltica.craft.core.plugins.VaultPlugin;

import java.io.IOException;

/**
 * 廃止前のクローバー情報を格納するストアです。
 * 以前は全プレイヤーの所有クローバーをアーカイブする機能を備えていましたが、
 * クローバー廃止に伴い廃止しました。クローバーをエビパワーに変換し次第
 * このクラスと関連クラスは廃止します。
 * @author Xeltica
 */
public class CloverStore {
    public CloverStore() {
        CloverStore.instance = this;
        clovers = new Ticks.Config("clovers");
    }

    public static CloverStore getInstance() { return instance; }

    public double getCloverOf(OfflinePlayer p) {
        return clovers.getConf().getDouble(p.getUniqueId().toString());
    }

    public void set(Player player, double balance) {
        clovers.getConf().set(player.getUniqueId().toString(), balance);
    }

    public void delete(Player player) {
        clovers.getConf().set(player.getUniqueId().toString(), null);
    }

    public void save() throws IOException {
        clovers.save();
    }

    private VaultPlugin plugin() {
        return VaultPlugin.getInstance();
    }

    private static CloverStore instance;
    private final Ticks.Config clovers;
}
