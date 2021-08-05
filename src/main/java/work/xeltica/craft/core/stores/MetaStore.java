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

    // TODO: チェンジログをここではなく別ファイルに書いてそれを参照する。
    // やり方を調べる必要がある
    private String[] changeLog = {
        "処罰報告がチャットに流れるように",
        "古いX Phoneを使用したときの挙動を改善",
        "X Phoneを右クリック, ブロックタップ, 空中ロングタップでのみ使用できるように",
        "右クリックで発動するアイテムをオフハンドに持ちX Phoneを使用したときの挙動を改善",
        "promoコマンドで提示される残り時間の表記がおかしい問題を修正",
        "内部プログラムの最適化",
    };
    
    private static MetaStore instance;
}
