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
        final var conf = meta.getConf();
        final var currentVersion = conf.getString("version", null);
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
    private final String[] changeLog = {
            "採掘によってエビパワーを入手可能に(1日4000EPまで)",
            "繁殖によってエビパワーを入手可能に",
            "焼石製造機を作れないよう制限（丸石が生成される）",
            "X Phoneを含むいくつかのシーンに効果音を追加",
            "ナイトメアワールドにわかばプレイヤーが入れてしまう不具合を修正",
            "スケルトントラップでスポーンしたスケルトンホースを攻撃するとEPが減る不具合を修正",
            "内部セーブデータの保存サイクルを改善し、異常な頻度で起こるディスク書き込みを抑制",
    };

    private static MetaStore instance;
}
