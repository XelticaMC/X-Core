package work.xeltica.craft.core.modules.notification

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import work.xeltica.craft.core.modules.xphone.XphoneModule
import java.io.File
import java.io.FileReader
import java.util.UUID

object NotificationModule: ModuleBase() {
    lateinit var instance: NotificationModule
    private set

    private lateinit var confirmed: Config

    private const val FILE_NAME = "notification.json"
    private const val NOTIFICATION_ID = "notificationID"
    private const val TITLE = "title"
    private const val MESSAGE = "message"
    private const val GIFTS = "gifts"
    private const val EP = "ep"

    private const val RECEIVER = "receiver"
    private const val GIFT_MATERIAL_NAME = "materialName"
    private const val GIFT_CUSTOM_NAME = "customName"
    private const val GIFT_COUNT = "count"
    private const val GIFT_ENCHANTS = "enchants"
    private const val GIFT_LORE = "lore"
    private const val GIFT_ENCHANT_NAME = "name"
    private const val GIFT_ENCHANT_LEVEL = "level"

    private val notifications: MutableList<Notification> = mutableListOf()

    override fun onEnable() {
        instance = this
        confirmed = Config("confirmed")
        registerHandler(NotificationHandler())
        reload()
    }

    fun reload() {
        notifications.clear()
        load()
        pushNotificationAll()
    }

    /**
     * 指定したプレイヤーの未読通知を取得します。
     */
    fun getUnreadNotification(player: Player): List<Notification> {
        val playerNotification = notifications.toMutableList()
        val confirmedList = confirmed.conf.getStringList(player.uniqueId.toString())

        for (i in notifications) {
            if (confirmedList.contains(i.notificationID)) {
                playerNotification.remove(i)
                continue
            }
            if (i.receivePlayer == null) continue
            if (!i.receivePlayer.contains(player.uniqueId))
                playerNotification.remove(i)
        }

        return playerNotification
    }

    /**
     * 指定した通知を既読にします。
     */
    fun readNotification(player: Player, notification: Notification) {
        val confirmedList = confirmed.conf.getStringList(player.uniqueId.toString())
        val list = mutableListOf<String>()
        list.addAll(confirmedList)
        list.add(notification.notificationID)
        confirmed.conf.set(player.uniqueId.toString(), list)
        confirmed.save()
    }

    /**
     * 指定したプレイヤーに、未読通知を送信します。
     */
    fun pushNotificationTo(player: Player) {
        val unreadNotifications = getUnreadNotification(player)
        if (unreadNotifications.isEmpty()) return
        player.sendMessage("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}${unreadNotifications.count()}件の通知があります！！")
        unreadNotifications.forEach {
            player.sendMessage("${ChatColor.GREEN}・${ChatColor.RESET}${it.title}")
        }
        player.sendMessage("${ChatColor.LIGHT_PURPLE}X Phoneの「通知アプリ」から確認できます。")
        XphoneModule.playTritone(player)
    }

    /**
     * オンラインの全プレイヤーに未読通知を送信します。
     */
    fun pushNotificationAll() {
        Bukkit.getOnlinePlayers().forEach(::pushNotificationTo)
    }

    private fun load() {
        val folder = XCorePlugin.instance.dataFolder
        val file = File(folder, FILE_NAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        val jsonParser = JSONParser()
        val jsonObject = jsonParser.parse(FileReader(file, Charsets.UTF_8)) as? JSONArray ?: return
        for (obj in jsonObject) {
            if (obj !is JSONObject) continue
            loadNotification(obj)
        }
    }

    private fun loadNotification(obj: JSONObject) {
        val notificationID = obj[NOTIFICATION_ID] as? String ?: return
        val title = obj[TITLE] as? String ?: return
        val message = obj[MESSAGE] as? String ?: return
        XCorePlugin.instance.logger.info("通知" + title + "の読み込みをします")
        val giftsObject = obj[GIFTS] as? JSONArray
        val gifts = loadGiftItems(giftsObject)
        val ep = (obj[EP] as? Long)?.toInt()
        val receiverObject = obj[RECEIVER] as? JSONArray
        val receiver = loadReceiver(receiverObject)

        notifications.add(Notification(notificationID, title, message, gifts, ep, receiver))
    }

    private fun loadGiftItems(obj: JSONArray?): List<ItemStack>? {
        if (obj == null) return null
        val gifts = mutableListOf<ItemStack>()
        for (gift in obj) {
            if (gift !is JSONObject) continue
            val name = gift[GIFT_MATERIAL_NAME] as? String ?: continue
            val customName = gift[GIFT_CUSTOM_NAME] as? String
            val count = (gift[GIFT_COUNT] as? Long ?: 1L).toInt()
            val lore = (gift[GIFT_LORE] as? JSONArray)?.map {
                Component.text(it?.toString() ?: "")
            }

            val material = Material.getMaterial(name)
            if (material == null) {
                XCorePlugin.instance.logger.warning(name + "が読み込めませんでした")
                continue
            }
            val item = ItemStack(material, count)
            item.editMeta {
                it.lore(lore)
                if (customName !== null) {
                    it.displayName(Component.text(customName))
                }
                (gift[GIFT_ENCHANTS] as? JSONArray)?.forEach { enchant ->
                    if (enchant !is JSONObject) return@forEach
                    val name = enchant[GIFT_ENCHANT_NAME] as? String ?: return@forEach
                    val level = (enchant[GIFT_ENCHANT_LEVEL] as? Long ?: return@forEach).toInt()
                    val type = Enchantment.getByKey(NamespacedKey.minecraft(name)) ?: return@forEach
                    it.addEnchant(type, level, true)
                }
            }
            gifts.add(item)
        }
        return gifts
    }

    private fun loadReceiver(obj: JSONArray?): List<UUID>? {
        if (obj == null) return null
        val receiver = mutableListOf<UUID>()
        for (name in obj) {
            if (name !is String) continue
            val player = XCorePlugin.instance.server.getPlayerUniqueId(name) ?: continue
            receiver.add(player)
        }
        return receiver
    }
}
