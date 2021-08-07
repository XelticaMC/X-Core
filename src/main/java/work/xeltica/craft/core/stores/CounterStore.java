package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import lombok.Getter;
import work.xeltica.craft.core.models.CounterData;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.utils.Config;

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

    /**
     * IDを用いてCounterDataを取得します。
     * @param id CounterData ID
     * @return I対応するCounterData。なければnull
     */
    public @Nullable CounterData get(String id) {
        return counters.get(id);
    }

    /**
     * 名前を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    public @Nullable CounterData getByName(String name) {
        return nameIndex.get(name);
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
     * 終点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    public @Nullable String getIdOf(CounterData data) {
        return idIndex.get(data);
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
     * @return 追加したデータのID。
     * @throws IOException 保存に失敗した
     */
    public String add(CounterData data) throws IOException {
        final var id = UUID.randomUUID().toString();

        counters.put(id, data);
        addToIndex(data, id);
        config.getConf().set(id, data);
        config.save();

        return id;
    }

    /**
     * CounterDataを削除します。
     * @param data 削除するデータ
     * @throws IOException 保存に失敗した
     */
    public void remove(CounterData data) throws IOException {
        remove(getIdOf(data));
    }

    /**
     * CounterDataを削除します。
     * @param id 削除するデータ ID
     * @throws IOException 保存に失敗した
     */
    public void remove(String id) throws IOException {
        removeFromIndex(get(id));
        counters.remove(id);
        config.getConf().set(id, null);
        config.save();
    }

    /**
     * 全プレイヤーのデイリーイベントプレイ履歴を削除します。
     * @throws IOException 保存に失敗した
     */
    public void resetAllPlayersPlayedLog() throws IOException {
        final var pstore = PlayerStore.getInstance();
        pstore.openAll()
            .forEach(record -> record.delete(PlayerDataKey.PLAYED_COUNTER));
        pstore.save();
    }
    
    /**
     * インデックスにカウンターデータを追加する
     */
    private void addToIndex(CounterData data, String id) {
        nameIndex.put(data.getName(), data);
        location1Index.put(data.getLocation1(), data);
        location2Index.put(data.getLocation2(), data);
        idIndex.put(data, id);
    }
    
    /**
     * インデックスからカウンターデータを削除する
     */
    private void removeFromIndex(CounterData data) {
        nameIndex.remove(data.getName());
        location1Index.remove(data.getLocation1());
        location2Index.remove(data.getLocation2());
        idIndex.remove(data);
    }

    private void loadAll() {
        final var yml = config.getConf();
        yml.getKeys(false).forEach(id -> {
            final var counter = yml.getObject(id, CounterData.class);
            counters.put(id, counter);
            addToIndex(counter, id);
        });
    }

    @Getter
    private static CounterStore instance;

    /** counters.yml */
    private Config config;

    /** カウンターデータのマップ */
    private final Map<String, CounterData> counters = new HashMap<>();

    /** IDに紐づくカウンターデータの一覧 */

    /** 名前による索引 */
    private final Map<String, CounterData> nameIndex = new HashMap<>();
    /** 始点座標による索引 */
    private final Map<Location, CounterData> location1Index = new HashMap<>();
    /** 終点座標による索引 */
    private final Map<Location, CounterData> location2Index = new HashMap<>();
    /** IDの索引 */
    private final Map<CounterData, String> idIndex = new HashMap<>();
}
