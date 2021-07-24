package work.xeltica.craft.core.stores;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.bossbar.BossBar;

/**
 * 全体向けに表示するBossBarを管理します。
 */
public class BossBarStore {
    public BossBarStore() {
        BossBarStore.instance = this;
    }

    public static BossBarStore getInstance() {
        return BossBarStore.instance;
    }

    public void add(BossBar bar) {
        bossBars.add(bar);
        Bukkit.getServer().audiences().forEach(a -> {
            a.showBossBar(bar);
        });
    }

    public void remove(BossBar bar) {
        bossBars.remove(bar);
        Bukkit.getServer().audiences().forEach(a -> {
            a.hideBossBar(bar);
        });
    }

    public void applyAll(Player p) {
        bossBars.forEach(bar -> {
            p.showBossBar(bar);
        });
    }

    private ArrayList<BossBar> bossBars = new ArrayList<>();
    
    private static BossBarStore instance;
}