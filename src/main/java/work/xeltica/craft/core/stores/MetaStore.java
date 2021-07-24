package work.xeltica.craft.core.stores;

import java.io.IOException;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.utils.Config;

public class MetaStore {
    public MetaStore() {
        MetaStore.instance = this;
        meta = new Config("meta");
        checkUpdate();
    }

    public static MetaStore getInstance() {
        return instance;
    }

    public String getCurrentVersion() {
        return XCorePlugin.getInstance().getDescription().getVersion();
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String[] getChangeLog() {
        return changeLog;
    }

    private void checkUpdate() {
        var conf = meta.getConf();
        var currentVersion = conf.getString("version", null);
        previousVersion = conf.getString("previousVersion", null);
        if (currentVersion == null || !currentVersion.equals(getCurrentVersion())) {
            conf.set("version", getCurrentVersion());
            conf.set("previousVersion", currentVersion);
            previousVersion = currentVersion;
            isUpdated = true;
            try {
                meta.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private final Config meta;
    private String previousVersion;
    private boolean isUpdated;

    private String[] changeLog = {
        "参加時に、ヒント「晴れて市民になった！」を達成していない市民は達成できるように",
        "子供、ペット、ネコを殴った場合のヒントが達成できない不具合を修正",
    };
    
    private static MetaStore instance;
}
