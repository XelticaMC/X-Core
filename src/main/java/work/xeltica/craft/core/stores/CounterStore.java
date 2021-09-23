package work.xeltica.craft.core.stores;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import work.xeltica.craft.core.models.CounterData;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.utils.Config;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 時間計測カウンターの情報を管理します。
 */
public class CounterStore {
    public CounterStore() {
        ConfigurationSerialization.registerClass(CounterData.class, "CounterData");
        instance = this;
        config = new Config("counters");
        loadAll();
    }

    public static CounterStore getInstance() {
        return CounterStore.instance;
    }

    /**
     * 名前を用いてCounterDataを取得します。
     * @param name CounterDataの名前
     * @return 対応するCounterData。なければnull
     */
    public @Nullable CounterData get(String name) {
        return counters.get(name);
    }

    /**
     * 始点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    public @Nullable CounterData getByLocation1(Location location) {
        return location1Index.get(location);
    }

    /**
     * 終点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    public @Nullable CounterData getByLocation2(Location location) {
        return location2Index.get(location);
    }

    /**
     * カウンターの一覧を取得します。
     * @return カウンターの一覧。
     */
    public List<CounterData> getCounters() {
        return counters.values().stream().toList();
    }

    /**
     * CounterDataを追加します。
     * @param data 追加するデータ
     * @throws IOException 保存に失敗した
     */
    public void add(CounterData data) throws IOException {
        counters.put(data.getName(), data);
        addToIndex(data);
        update(data);
    }

    /**
     * CounterDataを削除します。
     * @param data 削除するデータ
     * @throws IOException 保存に失敗した
     */
    public void remove(CounterData data) throws IOException {
        remove(data.getName());
    }

    public void update(CounterData data) throws IOException {
        if (!counters.containsKey(data.getName())) throw new IllegalArgumentException();
        config.getConf().set(data.getName(), data);
        config.save();
    }

    /**
     * CounterDataを削除します。
     * @param name 削除するデータの名前
     * @throws IOException 保存に失敗した
     */
    public void remove(String name) throws IOException {
        removeFromIndex(get(name));
        counters.remove(name);
        config.getConf().set(name, null);
        config.save();
    }

    /**
     * 全プレイヤーのデイリーイベントプレイ履歴を削除します。
     * @throws IOException 保存に失敗した
     */
    public void resetAllPlayersPlayedLog() throws IOException {
        final var pstore = PlayerStore.getInstance();
        pstore.openAll()
            .forEach(record -> record.delete(PlayerDataKey.PLAYED_COUNTER, false));
        pstore.save();
    }

    /**
     * インデックスにカウンターデータを追加する
     */
    private void addToIndex(CounterData data) {
        location1Index.put(data.getLocation1(), data);
        location2Index.put(data.getLocation2(), data);
    }

    /**
     * インデックスからカウンターデータを削除する
     */
    private void removeFromIndex(CounterData data) {
        location1Index.remove(data.getLocation1());
        location2Index.remove(data.getLocation2());
    }

    private void loadAll() {
        final var yml = config.getConf();
        yml.getKeys(false).forEach(name -> {
            final var counter = yml.getObject(name, CounterData.class);
            counters.put(name, counter);
            addToIndex(counter);
        });
    }

    private static CounterStore instance;

    /** counters.yml */
    private Config config;

    /** カウンターデータのマップ */
    private final Map<String, CounterData> counters = new HashMap<>();

    /** 始点座標による索引 */
    private final Map<Location, CounterData> location1Index = new HashMap<>();
    /** 終点座標による索引 */
    private final Map<Location, CounterData> location2Index = new HashMap<>();
}
