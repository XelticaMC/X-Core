package work.xeltica.craft.core.models

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack

/**
 * アメストアで取り扱う商品を表すモデルです。
 */
data class CandyStoreItem(val item: ItemStack, val cost: Int) : Cloneable, ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result["item"] = item.serialize()
        result["cost"] = cost
        return result
    }

    companion object {
        @JvmStatic
        fun deserialize(args: Map<String?, Any?>): EbiPowerItem {
            val item = ItemStack.deserialize((args["item"] as Map<String?, Any?>?)!!)
            val cost = args["cost"] as Int
            return EbiPowerItem(item, cost)
        }
    }
}