package work.xeltica.craft.core.modules.stamprally

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import java.util.UUID

/**
 * スタンプラリー機能を提供するモジュールです。
 */
object StampRallyModule : ModuleBase() {
    const val CREATE_PERMISSION = "otanoshimi.stamp.create"
    const val DESTROY_PERMISSION = "otanoshimi.stamp.destroy"

    private lateinit var stampList: Config
    private lateinit var activatedStamp: Config

    override fun onEnable() {
        stampList = Config("stampList")
        activatedStamp = Config("activatedStamp")

        registerHandler(StampRallyHandler())
        registerCommand("stamp", StampCommand())
    }

    /**
     * スタンプラリーの周回が完了したプレイヤーの一覧を取得します。
     */
    fun getDonePlayerList(): List<OfflinePlayer> {
        val keys = activatedStamp.conf.getKeys(false)
        val players = mutableListOf<OfflinePlayer>()
        for (uuid in keys) {
            val player = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
            val stamp = HashSet(getEntireStampList())
            stamp.removeAll(activatedStamp.conf.getStringList(uuid).toSet())
            if (stamp.isEmpty()) players.add(player)
        }
        return players
    }

    /**
     * [player] が スタンプ [stampName] を達成したとマークします。
     */
    fun activate(player: Player, stampName: String) {
        if (!contains(stampName)) return

        val activated = activatedStamp.conf.getStringList(player.uniqueId.toString())

        if (activated.contains(stampName)) {
            player.sendMessage(stampName + "はもう押されています！")
            return
        }

        player.sendMessage(stampName + "のスタンプを押しました!")
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
        val list = mutableListOf<String>()
        list.addAll(activated)
        list.add(stampName)
        activatedStamp.conf.set(player.uniqueId.toString(), list)
        activatedStamp.save()

        if (isStampAchieved(player)) {
            player.sendMessage("ラリー達成おめでとうございます！5000EPを贈呈します")
            EbiPowerModule.tryGive(player, 5000)
        }
    }

    /**
     * [player] がスタンプラリーを達成したかどうかを取得します。
     */
    fun isStampAchieved(player: Player): Boolean {
        val entireStamp = HashSet(getEntireStampList())
        val activated = activatedStamp.conf.getStringList(player.uniqueId.toString())
        entireStamp.removeAll(activated.toSet())
        return entireStamp.isEmpty()
    }

    /**
     * 全てのスタンプ一覧を取得します。
     */
    fun getEntireStampList(): List<String> {
        return stampList.conf.getStringList("stamps")
    }

    /**
     * [player] が達成したスタンプの一覧を取得します。
     */
    fun getActivatedStampList(player: Player): List<String> {
        return activatedStamp.conf.getStringList(player.uniqueId.toString())
    }

    /**
     * [stampName] で表されるスタンプの詳細情報を取得します。
     */
    fun getStampInfo(stampName: String): Stamp {
        val data = stampList.conf.get(stampName)
        return Stamp.deserialize(data as MemorySection)
    }

    /**
     * スタンプを [stampName] という名前で、 [location] に新規作成します。
     */
    fun create(stampName: String, location: Location) {
        if (contains(stampName)) return
        val stamps = getEntireStampList()
        val list = mutableListOf<String>()
        list.addAll(stamps)
        list.add(stampName)
        stampList.conf.set("stamps", list)

        val stamp = Stamp(stampName, location)
        stampList.conf.set(stampName, stamp.serialize())
        stampList.save()
    }

    /**
     * スタンプ [stampName] が存在するかどうかを取得します。
     */
    fun contains(stampName: String): Boolean {
        val stamps = getEntireStampList()
        return stamps.contains(stampName)
    }

    /**
     * スタンプ [stampName] を削除します。
     */
    fun destroy(stampName: String) {
        if (!contains(stampName)) return
        val stamps = stampList.conf.getStringList("stamps")
        stamps.remove(stampName)
        stampList.conf.set("stamps", stamps)
        stampList.conf.set(stampName, null)
        stampList.save()
    }
}