package work.xeltica.craft.core.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import work.xeltica.craft.core.api.Config;
import work.xeltica.craft.core.api.Time;
import work.xeltica.craft.core.stores.RankingStore;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        this.name = name;
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
        setDisplayName(name, true);
    }

    /**
     * このランキングの表示名を設定します。
     * @param name ランキングの表示名
     * @throws IOException 保存に失敗
     */
    public void setDisplayName(String name, boolean save) throws IOException {
        set("displayName", name, save);
        RankingStore.getInstance().renderAll();
    }

    /**
     * このランキングがプレイヤーをキーとするかどうかを取得します。
     * @return プレイヤーをキーとするランキングであればtrue、そうでなければfalse。
     */
    public boolean isPlayerMode() {
        return thisSection.getBoolean("isPlayerMode");
    }

    /**
     * モードを設定します。
     * @param mode normal, point, time のどちらか
     * @throws IOException 保存に失敗
     */
    public void setMode(String mode) throws IOException {
        set("mode", mode);
        RankingStore.getInstance().renderAll();
    }

    /**
     * モードを取得します。
     * @return normal, point, time のどちらか
     */
    public String getMode() {
        return thisSection.getString("mode", "normal");
    }

    /**
     * このランキングがプレイヤーをキーとするかどうかを設定します。
     * @param value プレイヤーをキーとするランキングであるかどうか
     * @throws IOException 保存に失敗
     */
    public void setIsPlayerMode(boolean value) throws IOException {
        setIsPlayerMode(value, true);
    }

    /**
     * このランキングがプレイヤーをキーとするかどうかを設定します。
     * @param value プレイヤーをキーとするランキングであるかどうか
     * @throws IOException 保存に失敗
     */
    public void setIsPlayerMode(boolean value, boolean save) throws IOException {
        set("isPlayerMode", value, save);
        RankingStore.getInstance().renderAll();
    }

    public Location getHologramLocation() {
        return thisSection.getLocation("hologramLocation");
    }

    public boolean getHologramHidden() {
        return thisSection.getBoolean("hologramHidden");
    }

    public void setHologram(Location location, boolean isHidden) throws IOException {
        set("hologramLocation", location, false);
        set("hologramHidden", isHidden, false);
        save();
        RankingStore.getInstance().renderAll();
    }

    public void save() throws IOException {
        conf.save();
    }

    /**
     * 指定したレコードIDのレコードの値を取得します。
     * @return 順位でソートされたレコード
    */
    public int get(String id) {
        return records.getOrDefault(id, 0);
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
        final var stream = records.entrySet().stream();
        final var data = (switch (getMode()) {
            case "normal" ->
                stream
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(e -> {
                        var key = e.getKey();
                        if (isPlayerMode()) {
                            final var player = Bukkit.getOfflinePlayer(UUID.fromString(key));
                            if (player != null) key = player.getName();
                        }
                        return new RankingRecord(key, e.getValue().toString());
                    });
            case "time" ->
                stream
                    .sorted(Map.Entry.comparingByValue())
                    .map(e -> {
                        var key = e.getKey();
                        if (isPlayerMode()) {
                            final var player = Bukkit.getOfflinePlayer(UUID.fromString(key));
                            if (player != null) key = player.getName();
                        }
                        return new RankingRecord(key, Time.msToString(e.getValue()));
                    });
            case "point" ->
                stream
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(e -> {
                        var key = e.getKey();
                        if (isPlayerMode()) {
                            final var player = Bukkit.getOfflinePlayer(UUID.fromString(key));
                            if (player != null) key = player.getName();
                        }
                        return new RankingRecord(key, e.getValue().toString() + "点");
                    });
            default -> throw new IllegalArgumentException();
        }).toArray(RankingRecord[]::new);
        return data.length > count ? Arrays.copyOf(data, count) : data;
    }

    /**
     * ランキングに指定IDのレコードを追加します。
     * @param id レコードID。
     * @param score スコア。
    */
    public void add(String id, int score) {
        records.put(id, score);
        saveRecords();
        RankingStore.getInstance().renderAll();
    }

    /**
     * ランキングから指定IDのレコードを削除します。
     * @param id レコードID。
    */
    public void remove(String id) {
        records.remove(id);
        saveRecords();
        RankingStore.getInstance().renderAll();
    }

    /** 値をセットし、ディスクに保存する
     * @param key セットする値のキー。
     * @param value セットする値
     * @throws IOException 保存に失敗
    */
    private void set(String key, Object value) throws IOException {
        set(key, value, true);
    }

    /** 値をセットし、ディスクに保存する
     * @param key セットする値のキー。
     * @param value セットする値
     * @throws IOException 保存に失敗
    */
    private void set(String key, Object value, boolean save) throws IOException {
        thisSection.set(key, value);
        if (save) {
            conf.save();
        }
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
        // 全レコードを最初から書き込むので残ってるやつを消す
        final var r = thisSection.getConfigurationSection("records");
        final var recordsSection = r != null ? r : thisSection.createSection("records");
        recordsSection.getKeys(false).forEach(k -> recordsSection.set(k, null));

        records.keySet().forEach(id -> {
            recordsSection.set(id, records.get(id));
        });

        try {
            conf.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String name;

    private HashMap<String, Integer> records;
    private ConfigurationSection thisSection;
    private Config conf;

    public String getName() {
        return this.name;
    }
}
