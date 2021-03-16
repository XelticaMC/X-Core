package work.xeltica.craft.otanoshimiplugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerHideManager {
    public PlayerHideManager(Plugin pl) {
        this.plugin = pl;
        PlayerHideManager.instance = this;
        logger = Bukkit.getLogger();
    }

    public static PlayerHideManager getInstance() {
        return PlayerHideManager.instance;
    }

    public void updateAll(Player pl) {
        updateAll(pl, Bukkit.getOnlinePlayers());
    }

    public void update(Player pl, Player other) {
        var hides = pl.getMetadata("hidingPlayer");
        if (hides.size() == 0) return;
        var hide = hides.get(0).asBoolean();
        update(pl, other, hide);
    }

    public void updateAll(Player pl, Collection<? extends Player> players) {
        var hides = pl.getMetadata("hidingPlayer");
        if (hides.size() == 0)
            return;
        var hide = hides.get(0).asBoolean();
        players.forEach(p -> update(pl, p, hide));
    }

    public void update(Player pl, Player other, boolean hide) {
        if (hide) {
            pl.hidePlayer(plugin, other);
        } else {
            pl.showPlayer(plugin, other);
        }
    }
    
    private static PlayerHideManager instance;
    private Plugin plugin;
    private Logger logger;
}
