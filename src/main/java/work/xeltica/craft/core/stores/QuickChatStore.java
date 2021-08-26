package work.xeltica.craft.core.stores;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.utils.Config;

import java.io.IOException;
import java.util.Set;

/**
 * @author raink1208
 */
public class QuickChatStore {
    public QuickChatStore() {
        instance = this;
        XCorePlugin.getInstance().saveResource("quickChats.yml", false);
        config = new Config("quickChats");
    }

    public static QuickChatStore getInstance() { return instance; }

    public Set<String> getAllPrefix() {
        return config.getConf().getKeys(false);
    }

    public String getMessage(String prefix) {
        return config.getConf().getString(prefix);
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
    private final Config config;
}
