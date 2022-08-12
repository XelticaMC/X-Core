package work.xeltica.craft.core.stores;

import java.io.IOException;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.utils.Config;

/**
 * プラグインのメタ情報を管理します。
 * @author Xeltica
 */
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

    public boolean getPostToDiscord() {
        return postToDiscord;
    }

    public String[] getChangeLog() {
        return changeLog;
    }

    private void checkUpdate() {
        final var conf = meta.getConf();
        final var currentVersion = conf.getString("version", null);
        postToDiscord = conf.getBoolean("postToDiscord", false);
        if (!conf.contains("postToDiscord")) {
            conf.set("postToDiscord", false);
        }
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
    private boolean postToDiscord;

    // TODO: チェンジログをここではなく別ファイルに書いてそれを参照する。
    // やり方を調べる必要がある
    private final String[] changeLog = {
            "ネザークォーツ鉱石およびネザー金鉱石をエビパワーボーナスの対象に追加しました",
            "一部の条件で、不正にエビパワーを取得できてしまう問題を修正しました",
            "通知機能について細かな調整を施しました",
            "その他、細かな調整"
    };

    private static MetaStore instance;
}
