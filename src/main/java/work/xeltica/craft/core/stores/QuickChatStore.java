package work.xeltica.craft.core.stores;

import org.bukkit.entity.Player;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.Ticks;

import java.io.IOException;
import java.util.Set;

/**
 * @author raink1208
 */
public class QuickChatStore {
    public QuickChatStore() {
        instance = this;
        XCorePlugin.getInstance().saveResource("quickChats.yml", false);
        config = new Ticks.Config("quickChats");
    }

    public static QuickChatStore getInstance() { return instance; }

    public Set<String> getAllPrefix() {
        return config.getConf().getKeys(false);
    }

    public String getMessage(String prefix) {
        return config.getConf().getString(prefix);
    }

    public String chatFormat(String msg, Player player) {

        msg = msg.replace("{world}", WorldStore.getInstance().getWorldDisplayName(player.getWorld()));
        msg = msg.replace("{x}", String.valueOf(player.getLocation().getBlockX()));
        msg = msg.replace("{y}", String.valueOf(player.getLocation().getBlockY()));
        msg = msg.replace("{z}", String.valueOf(player.getLocation().getBlockZ()));

        return msg;
    }

    public boolean register(String prefix, String msg) {
        if (config.getConf().contains(prefix)) return false;

        config.getConf().set(prefix, msg);
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean unregister(String prefix) {
        if (config.getConf().contains(prefix)) {
            config.getConf().set(prefix, null);
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private static QuickChatStore instance;
    private final Ticks.Config config;
}
