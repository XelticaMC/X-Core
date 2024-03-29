package work.xeltica.craft.core.modules.ebipowerShop

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.utils.CastHelper

/**
 * エビパワーストアで購入可能なアイテムを表す、シリアライズ可能なデータクラス。
 */
data class EbiPowerItem(val item: ItemStack, val cost: Int) : Cloneable, ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result["item"] = item.serialize()
        result["cost"] = cost

        return result
    }

    companion object {
        // NOTE: Spigotは動的にこの関数を呼び出すため、JvmStaticでなければならない
        @JvmStatic
        fun deserialize(args: Map<String, Any>): EbiPowerItem {
            val item = ItemStack.deserialize(CastHelper.checkMap<String, Any>(args["item"] as Map<*, *>))
            val cost = args["cost"] as Int

            return EbiPowerItem(item, cost)
        }
    }
}
