package work.xeltica.craft.core.models;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import lombok.Getter;
import work.xeltica.craft.core.utils.Config;

/**
 * ランキングデータのインターフェイス
 * @author Xeltica
*/
public class Ranking {
    /**
     * Rankingクラスの新しいインスタンスを初期化します。
     * ディスクに存在しなければ自動作成し、存在すればそれを使用します。
     * @param name ランキング名
     * @param conf ランキングが保存されるコンフィグデータ
     */
    public Ranking(String name, Config conf) {
        this.conf = conf;
        loadSection();
        loadRecords();
    }

    /**
     * このランキングの表示名を取得します。
     * @return ランキングの表示名
     */
    public String getDisplayName() {
        return thisSection.getString("displayName");
    }

    /**
     * このランキングの表示名を設定します。
     * @param name ランキングの表示名
     * @throws IOException 保存に失敗
     */
    public void setDisplayName(String name) throws IOException {
        set("displayName", name);
    }

    /**
     * このランキングがプレイヤーをキーとするかどうかを取得します。
     * @return プレイヤーをキーとするランキングであればtrue、そうでなければfalse。
     */
    public boolean isPlayerMode() {
        return thisSection.getBoolean("isPlayerMode");
    }

    /**
     * このランキングがプレイヤーをキーとするかどうかを設定します。
     * @param name プレイヤーをキーとするランキングであるかどうか
     * @throws IOException 保存に失敗
     */
    public void setIsPlayerMode(boolean value) throws IOException {
        set("isPlayerMode", value);
    }

    /**
     * 指定したレコードIDのレコードの値を取得します。
     * @return 順位でソートされたレコード
    */
    public int get(String id) {
        return records.get(id);
    }

    /**
     * 指定した数のランキングを10件取得します。
     * @return 順位でソートされたレコード
    */
    public RankingRecord[] queryRanking() {
        return queryRanking(10);
    }

    /**
     * 指定した数のランキングを取得します。
     * @param count ランキングとして取得するレコード数
     * @return 順位でソートされたレコード
    */
    public RankingRecord[] queryRanking(int count) {
        return records.entrySet().stream()
            .map(e -> {
                var key = e.getKey();
                if (isPlayerMode()) {
                    final var player = Bukkit.getOfflinePlayer(UUID.fromString(key));
                    if (player != null) key = player.getName();
                }
                return new RankingRecord(key, e.getValue());
            })
            .sorted(Comparator.comparingInt(RankingRecord::score).reversed())
            .toArray(RankingRecord[]::new);
    }

    /**
     * ランキングに指定IDのレコードを追加します。
     * @param id レコードID。
     * @param score スコア。
    */
    public void add(String id, int score) {
        records.put(id, score);
        saveRecords();
    }

    /**
     * ランキングから指定IDのレコードを削除します。
     * @param id レコードID。
    */
    public void remove(String id) {
        records.remove(id);
    }

    /** 値をセットし、ディスクに保存する
     * @param key セットする値のキー。
     * @param value セットする値
     * @throws IOException 保存に失敗
    */
    private void set(String key, Object value) throws IOException {
        thisSection.set(key, value);
        conf.save();
        saveRecords();
    }

    /** 与えられたconfからこのランキングのセクションを読み込む or 作成する */
    private void loadSection() {
        final var s = conf.getConf().getConfigurationSection(name);
        thisSection = s == null ? conf.getConf().createSection(name) : s;
    }

    /** ディスクからレコードを読み込む */
    private void loadRecords() {
        var recordsSection = thisSection.getConfigurationSection("records");
        records = new HashMap<>();

        if (recordsSection != null) {
            recordsSection.getKeys(false).forEach(key -> records.put(key, recordsSection.getInt(key)));
        }
    }

    /** ディスクにレコードを書き込む */
    private void saveRecords() {
        records.keySet().forEach(id -> {
            var recordsSection = thisSection.getConfigurationSection("records");
            if (recordsSection == null) recordsSection = thisSection.createSection("records");
            recordsSection.set(id, records.get(id));
        });
        try {
            conf.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Getter
    private String name;

    private HashMap<String, Integer> records;
    private ConfigurationSection thisSection;
    private Config conf;
}
