package work.xeltica.craft.core.modules

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerialization
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.models.CounterData
import work.xeltica.craft.core.modules.PlayerStoreModule.openAll
import work.xeltica.craft.core.modules.PlayerStoreModule.save
import kotlin.Throws
import java.io.IOException
import work.xeltica.craft.core.models.PlayerRecord
import work.xeltica.craft.core.models.PlayerDataKey
import java.util.HashMap
import java.util.function.Consumer

/**
 * 時間計測カウンターの情報を管理します。
 */
object CounterModule : ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(CounterData::class.java, "CounterData")
        config = Config("counters")
        loadAll()
    }

    /**
     * 名前を用いてCounterDataを取得します。
     * @param name CounterDataの名前
     * @return 対応するCounterData。なければnull
     */
    @JvmStatic
    operator fun get(name: String): CounterData? {
        return counters[name]
    }

    /**
     * 始点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    @JvmStatic
    fun getByLocation1(location: Location): CounterData? {
        return location1Index[location]
    }

    /**
     * 終点座標を用いてCounterDataを取得します。
     * @return 対応するCounterData。なければnull
     */
    @JvmStatic
    fun getByLocation2(location: Location): CounterData? {
        return location2Index[location]
    }

    /**
     * カウンターの一覧を取得します。
     * @return カウンターの一覧。
     */
    @JvmStatic
    fun getCounters(): List<CounterData> {
        return counters.values.stream().toList()
    }

    /**
     * CounterDataを追加します。
     * @param data 追加するデータ
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    @JvmStatic
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
    @JvmStatic
    fun remove(data: CounterData) {
        remove(data.name)
    }

    @Throws(IOException::class)
    @JvmStatic
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
    @JvmStatic
    fun remove(name: String) {
        removeFromIndex(get(name))
        counters.remove(name)
        config.conf[name] = null
        config.save()
    }

    /**
     * 全プレイヤーのデイリーイベントプレイ履歴を削除します。
     * @throws IOException 保存に失敗した
     */
    @Throws(IOException::class)
    @JvmStatic
    fun resetAllPlayersPlayedLog() {
        openAll()
            .forEach(Consumer { record: PlayerRecord -> record.delete(PlayerDataKey.PLAYED_COUNTER_COUNT, false) })
        save()
    }

    /**
     * インデックスにカウンターデータを追加する
     */
    private fun addToIndex(data: CounterData) {
        location1Index[data.location1] = data
        location2Index[data.location2] = data
    }

    /**
     * インデックスからカウンターデータを削除する
     */
    private fun removeFromIndex(data: CounterData?) {
        location1Index.remove(data!!.location1)
        location2Index.remove(data.location2)
    }

    private fun loadAll() {
        val yml = config.conf
        yml.getKeys(false).forEach(Consumer { name: String ->
            val counter = yml.getObject(name, CounterData::class.java)!!
            counters[name] = counter
            addToIndex(counter)
        })
    }

    /** counters.yml  */
    private lateinit var config: Config

    /** カウンターデータのマップ  */
    private val counters: MutableMap<String, CounterData> = HashMap()

    /** 始点座標による索引  */
    private val location1Index: MutableMap<Location, CounterData> = HashMap()

    /** 終点座標による索引  */
    private val location2Index: MutableMap<Location, CounterData> = HashMap()
}