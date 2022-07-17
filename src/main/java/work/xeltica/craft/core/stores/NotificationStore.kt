package work.xeltica.craft.core.stores

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.models.Notification
import work.xeltica.craft.core.utils.Config
import java.io.File
import java.io.FileReader
import java.util.UUID

class NotificationStore {
    companion object {
        private lateinit var instance: NotificationStore

        fun getInstance(): NotificationStore = instance

        private const val FILE_NAME = "notification.json"

        private const val NOTIFICATION_ID = "notificationID"
        private const val MESSAGE = "message"
        private const val GIFTS = "gifts"
        private const val EP = "ep"
        private const val RECEIVER = "receiver"

        private const val GIFT_MATERIAL_NAME = "materialName"
        private const val GIFT_COUNT = "count"
    }

    private val notifications: MutableList<Notification> = mutableListOf()

    private val confirmed = Config("confirmed")

    init {
        instance = this
        load()
    }

    fun getUnreadNotification(player: Player): List<Notification> {
        val playerNotification = notifications.toMutableList()
        playerNotification.removeAll { it.receivePlayer?.contains(player.uniqueId) ?: false }
        val confirmedNotification = confirmed.conf.getList(player.uniqueId.toString())
        playerNotification.removeAll { confirmedNotification?.contains(it.notificationID) ?: false }
        return playerNotification
    }

    private fun load() {
        val folder = XCorePlugin.instance.dataFolder
        val file = File(folder, FILE_NAME)
        val jsonParser = JSONParser()
        val jsonObject = jsonParser.parse(FileReader(file)) as? JSONArray ?: return
        for (obj in jsonObject) {
            if (obj !is JSONObject) continue
            loadNotification(obj)
        }
    }

    private fun loadNotification(obj: JSONObject) {
        val notificationID = obj[NOTIFICATION_ID] as? String ?: return
        val message = obj[MESSAGE] as? String ?: return
        val giftsObject = obj[GIFTS] as? JSONArray
        val gifts = loadGiftItems(giftsObject)
        val ep = obj[EP] as? Int
        val receiverObject = obj[RECEIVER] as? JSONArray
        val receiver = loadReceiver(receiverObject)

        notifications.add(Notification(notificationID, message, gifts, ep, receiver))
    }

    private fun loadGiftItems(obj: JSONArray?): List<ItemStack>? {
        if (obj == null) return null
        val gifts = mutableListOf<ItemStack>()
        for (gift in obj) {
            if (gift !is JSONObject) continue
            val name = gift[GIFT_MATERIAL_NAME] as? String ?: continue
            val count = gift[GIFT_COUNT] as? Int ?: continue
            val material = Material.getMaterial(name) ?: continue
            gifts.add(ItemStack(material, count))
        }
        return gifts
    }

    private fun loadReceiver(obj: JSONArray?): List<UUID>? {
        if (obj == null) return null
        val receiver = mutableListOf<UUID>()
        for (name in obj) {
            if (name !is String) continue
            val player = XCorePlugin.instance.server.getPlayer(name) ?: continue
            receiver.add(player.uniqueId)
        }
        return receiver
    }
}
