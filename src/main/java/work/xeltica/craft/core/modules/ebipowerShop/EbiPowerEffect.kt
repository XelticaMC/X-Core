package work.xeltica.craft.core.modules.ebipowerShop

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import work.xeltica.craft.core.utils.Ticks

data class EbiPowerEffect(val effectType: PotionEffectType, val level: Int, val time: Int, val cost: Int) :Cloneable, ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result["effectType"] = effectType.name
        result["level"] = level - 1
        result["time"] = time
        result["cost"] = cost

        return result
    }

    fun toPotionEffect(): PotionEffect {
        return PotionEffect(effectType, Ticks.from(time.toDouble()), level)
    }

    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>): EbiPowerEffect {
            val effectType = PotionEffectType.getByName(args["effectType"].toString())

            val level = args["level"] as Int
            val time = args["time"] as Int
            val cost = args["cost"] as Int

            if (effectType == null) throw IllegalArgumentException()

            return EbiPowerEffect(effectType, level, time, cost)
        }
    }
}