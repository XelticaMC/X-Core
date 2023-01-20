package work.xeltica.craft.core.modules.notification

import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * 通知データを表すデータクラスです。
 */
data class Notification(
    /**
     * 通知 ID。既読管理に使用します。
     */
    val notificationID: String,

    /**
     * この通知のタイトル。
     */
    val title: String,

    /**
     * この通知の本文。
     */
    val message: String,

    /**
     * この通知を開封すると貰えるアイテム。
     */
    val giftItems: List<ItemStack>?,

    /**
     * この通知を開封すると付与されるエビパワー。
     */
    val ep: Int?,

    /**
     * この通知の受信対象となるプレイヤーのリスト。
     */
    val receivePlayer: List<UUID>?,
)
