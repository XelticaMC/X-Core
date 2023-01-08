package work.xeltica.craft.core.modules.counter

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import work.xeltica.craft.core.utils.CastHelper

/**
 * タイムアタックカウンターの設置情報を保持する、シリアライズ可能なクラスです。
 */
class CounterData(
    val name: String, val location1: Location, val location2: Location, val isDaily: Boolean,
    var javaRankingId: String?, var bedrockRankingId: String?, var uwpRankingId: String?, var phoneRankingId: String?,
) : Cloneable, ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any?> {
        val serialized = HashMap<String, Any?>()
        serialized["name"] = name
        serialized["location1"] = location1.serialize()
        serialized["location2"] = location2.serialize()
        serialized["isDaily"] = isDaily
        serialized["javaRankingId"] = javaRankingId
        serialized["bedrockRankingId"] = bedrockRankingId
        serialized["uwpRankingId"] = uwpRankingId
        serialized["phoneRankingId"] = phoneRankingId
        return serialized
    }

    companion object {
        // NOTE: Spigotは動的にこの関数を呼び出すため、JvmStaticでなければならない
        @JvmStatic
        fun deserialize(args: Map<String, Any>): CounterData {
            assertKey(args, "name")
            assertKey(args, "location1")
            assertKey(args, "location2")
            assertKey(args, "isDaily")

            val name = args["name"] as String
            val location1 = Location.deserialize(CastHelper.checkMap<String, Any>(args["location1"] as Map<*, *>))
            val location2 = Location.deserialize(CastHelper.checkMap<String, Any>(args["location2"] as Map<*, *>))
            val isDaily = args["isDaily"] as Boolean

            val javaRankingId = args["javaRankingId"] as? String?
            val bedrockRankingId = args["bedrockRankingId"] as? String?
            val uwpRankingId = args["uwpRankingId"] as? String?
            val phoneRankingId = args["phoneRankingId"] as? String?

            return CounterData(name, location1, location2, isDaily, javaRankingId, bedrockRankingId, uwpRankingId, phoneRankingId)
        }

        fun assertKey(args: Map<String, Any>, key: String) {
            require(args.containsKey(key)) { "$key is null" }
        }
    }
}