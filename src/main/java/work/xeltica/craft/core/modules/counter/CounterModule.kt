package work.xeltica.craft.core.modules.counter

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerialization
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import java.io.IOException
import java.util.*

/**
 * タイムアタック機能と、それを実行するためのカウンターを設置する機能を提供するモジュールです。
 */
object CounterModule : ModuleBase() {
    lateinit var config: Config

    const val PS_KEY_MODE = "counter_register_mode"
    const val PS_KEY_NAME = "counter_register_name"
    const val PS_KEY_IS_DAILY = "counter_register_is_daily"
    const val PS_KEY_LOCATION = "counter_register_location"
    const val PS_KEY_ID = "counter_id"
    const val PS_KEY_TIME = "counter_time"
    const val PS_KEY_COUNT = "counter_count"

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
        TimeAttackObserver().runTaskTimer(XCorePlugin.instance, 0, 5)
    }

    /**
     * [name] という名前の [CounterData] を取得します。
     */
    operator fun get(name: String): CounterData? {
        return counters[name]
    }

    /**
     * [location] を始点とする [CounterData] を取得します。
     */
    fun getByLocation1(location: Location): CounterData? {
        return location1Index[location]
    }

    /**
     * [location] を終点とする [CounterData] を取得します。
     */
    fun getByLocation2(location: Location): CounterData? {
        return location2Index[location]
    }

    /**
     * [CounterData] の一覧を取得します。
     */
    fun getCounters(): List<CounterData> {
        return counters.values.stream().toList()
    }

    /**
     * [CounterData] を新規追加します。
     */
    @Throws(IOException::class)
    fun add(data: CounterData) {
        counters[data.name] = data
        addToIndex(data)
        update(data)
    }

    /**
     * [CounterData] を削除します。
     */
    @Throws(IOException::class)
    fun remove(data: CounterData) {
        remove(data.name)
    }

    /**
     * [CounterData] を更新します。
     */
    @Throws(IOException::class)
    fun update(data: CounterData) {
        require(counters.containsKey(data.name))
        config.conf[data.name] = data
        config.save()
    }

    /**
     * [CounterData] を削除します。
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
     */
    @Throws(IOException::class)
    fun resetAllPlayersPlayedLog() {
        PlayerStore.openAll().forEach { it.delete(PS_KEY_COUNT) }
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
    private fun removeFromIndex(data: CounterData) {
        location1Index.remove(data.location1)
        location2Index.remove(data.location2)
    }

    private fun loadAll() {
        val yml = config.conf
        for (name in yml.getKeys(false)) {
            val counter = yml.getObject(name, CounterData::class.java) as CounterData
            counters[name] = counter
            addToIndex(counter)
        }
    }
}