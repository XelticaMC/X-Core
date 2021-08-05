package work.xeltica.craft.core.stores;

import java.io.IOException;

import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import lombok.Getter;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.utils.Config;

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
        clovers = new Config("clovers");
    }

    public void saveAllCloversAccount() {
        final var eco = plugin().getEconomy();
        final var players = Lists.newArrayList(Bukkit.getOfflinePlayers());
        final var logger = Bukkit.getLogger();
        players.addAll(Bukkit.getOnlinePlayers());
        for (var p : players) {
            final var balance = eco.getBalance(p);
            if (balance == 0) continue;
            clovers.getConf().set(p.getUniqueId().toString(), balance);
            logger.info(String.format("%sさんの残高 %f Clover をデポジット", p.getName(), balance));
        }
        try {
            clovers.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getCloverOf(OfflinePlayer p) {
        return clovers.getConf().getDouble(p.getUniqueId().toString());
    }

    private VaultPlugin plugin() {
        return VaultPlugin.getInstance();
    }

    @Getter
    private static CloverStore instance;
    private final Config clovers;
}
