package work.xeltica.craft.core.modules.eventHalloween

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.utils.CastHelper

/**
 * アメストアで取り扱う商品を表すモデルです。
 */
data class CandyStoreItem(val item: ItemStack, override val cost: Int) : Cloneable, ConfigurationSerializable, ICandyStoreItem {
    override fun serialize(): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result["item"] = item.serialize()
        result["cost"] = cost
        return result
    }

    companion object {
        // NOTE: Spigotは動的にこの関数を呼び出すため、JvmStaticでなければならない
        @JvmStatic
        fun deserialize(args: Map<String?, Any?>): CandyStoreItem {
            val item = ItemStack.deserialize(CastHelper.checkMap<String, Any>(args["item"] as Map<*, *>))
            val cost = args["cost"] as Int
            return CandyStoreItem(item, cost)
        }
    }
}