package work.xeltica.craft.core.modules.ebipower

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.utils.CastHelper

data class EbiPowerItem(val item: ItemStack,val cost: Int): Cloneable, ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result["item"] = item.serialize()
        result["cost"] = cost

        return result
    }
    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>): EbiPowerItem {
            val item = ItemStack.deserialize(CastHelper.checkMap<String, Any>(args["item"] as Map<*, *>))
            val cost = args["cost"] as Int

            return EbiPowerItem(item, cost)
        }
    }
}
