package work.xeltica.craft.core.models

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import work.xeltica.craft.core.utils.Ticks

/**
 * エビパワードラッグストアの商品を表します。
 */
data class EbiPowerEffect(val effectType: PotionEffectType, val level: Int, val time: Int, val cost: Int)
    : Cloneable, ConfigurationSerializable {

    override fun serialize(): MutableMap<String, Any> {
        val result = HashMap<String, Any>()
        result["effectType"] = effectType
        result["level"] = level
        result["time"] = time
        result["cost"] = cost

        return result
    }

    fun toPotionEffect(): PotionEffect {
        return PotionEffect(effectType, Ticks.from(time.toDouble()), level)
    }

    companion object {
        @JvmStatic
        fun deserialize(args: MutableMap<String, Any>): EbiPowerEffect {
            val effectType = PotionEffectType.getByName((args["effectType"].toString()));

            val level = args["level"] as Int;
            val time = args["time"] as Int;
            val cost = args["cost"] as Int;

            if (effectType == null) throw IllegalArgumentException();

            return EbiPowerEffect(effectType, level, time, cost)
        }
    }
}
