package work.xeltica.craft.core.modules.counter

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerialization
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.models.PlayerRecord
import work.xeltica.craft.core.stores.PlayerStore
import work.xeltica.craft.core.utils.Config
import java.io.IOException

object CounterModule: ModuleBase() {
    lateinit var config: Config

    /** カウンターデータのマップ  */
    private val counters: HashMap<String, CounterData> = HashMap()

    /** 始点座標による索引  */
    private val location1Index: HashMap<Location, CounterData> = HashMap()

    /** 終点座標による索引  */
    private val location2Index: HashMap<Location, CounterData> = HashMap()


    override fun onEnable() {
        ConfigurationSerialization.registerClass(CounterData::class.java, "CounterData")
        config = Config("counters")
        loadAll()
        registerCommand("counter", CounterCommand())
        registerHandler(CounterHandler())
    }

    /**
     * 名前を用いてCounterDataを取得します。
     * @param name CounterDataの名前
     * @return 対応するCounterData。なければnull
     */
    operator fun get(name: String): CounterData? {
        return counters[name]
    }

    /**
     * 始点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    fun getByLocation1(location: Location): CounterData? {
        return location1Index[location]
    }

    /**
     * 終点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    fun getByLocation2(location: Location): CounterData? {
        return location2Index[location]
    }

    /**
     * カウンターの一覧を取得します。
     * @return カウンターの一覧。
     */
    fun getCounters(): List<CounterData> {
        return counters.values.stream().toList()
    }

    /**
     * CounterDataを追加します。
     * @param data 追加するデータ
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    fun add(data: CounterData) {
        counters[data.name] = data
        addToIndex(data)
        update(data)
    }

    /**
     * CounterDataを削除します。
     * @param data 削除するデータ
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    fun remove(data: CounterData) {
        remove(data.name)
    }

    @Throws(IOException::class)
    fun update(data: CounterData) {
        require(counters.containsKey(data.name))
        config.conf[data.name] = data
        config.save()
    }

    /**
     * CounterDataを削除します。
     * @param name 削除するデータの名前
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    fun remove(name: String) {
        val data = get(name) ?: return
        removeFromIndex(data)
        counters.remove(name)
        config.conf[name] = null
        config.save()
    }

    /**
     * 全プレイヤーのデイリーイベントプレイ履歴を削除します。
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    fun resetAllPlayersPlayedLog() {
        val pstore = PlayerStore.getInstance()
        pstore.openAll()
            .forEach{ record: PlayerRecord ->
                record.delete(
                    PlayerDataKey.PLAYED_COUNTER_COUNT
                )
            }
        pstore.save()
    }

    /**
     * インデックスにカウンターデータを追加する
     */
    fun addToIndex(data: CounterData) {
        location1Index[data.location1] = data
        location2Index[data.location2] = data
    }

    /**
     * インデックスからカウンターデータを削除する
     */
    fun removeFromIndex(data: CounterData) {
        location1Index.remove(data.location1)
        location2Index.remove(data.location2)
    }

    fun loadAll() {
        val yml = config.conf
        for (name in yml.getKeys(false)) {
            val counter = yml.getObject(name, CounterData::class.java) as CounterData
            counters[name] = counter
            addToIndex(counter)
        }
    }
}