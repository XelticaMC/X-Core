package work.xeltica.craft.core.models

import org.bukkit.inventory.ItemStack

data class Notification(val notificationID: String, val message: String, val giftItems: List<ItemStack>?, val ep: Int?)