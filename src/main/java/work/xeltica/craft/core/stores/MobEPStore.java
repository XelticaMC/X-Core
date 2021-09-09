package work.xeltica.craft.core.stores;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.utils.Config;

public class MobEPStore {
    public MobEPStore() {
        instance = this;
        XCorePlugin.getInstance().saveResource("mobEP.yml", false);
        config = new Config("mobEP.yml");
    }

    public static MobEPStore getInstance() { return instance; }

    private static MobEPStore instance;
    private static Config config;
}
