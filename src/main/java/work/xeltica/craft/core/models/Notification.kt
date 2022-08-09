package work.xeltica.craft.core.models

import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * プレイヤー向け通知のモデル。
 */
data class Notification(
    val notificationID: String,
    val title: String,
    val message: String,
    val giftItems: List<ItemStack>?,
    val ep: Int?,
    val receivePlayer: List<UUID>?
)