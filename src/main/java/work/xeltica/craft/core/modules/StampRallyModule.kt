package work.xeltica.craft.core.modules

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.models.Stamp
import java.util.UUID

object StampRallyModule : ModuleBase() {
    const val CREATE_PERMISSION = "otanoshimi.stamp.create"
    const val DESTROY_PERMISSION = "otanoshimi.stamp.destroy"

    private lateinit var stampList: Config
    private lateinit var activatedStamp: Config

    override fun onEnable() {
        stampList = Config("stampList")
        activatedStamp = Config("activatedStamp")
    }

    @JvmStatic
    fun getDonePlayerList(): List<Player> {
        val keys = activatedStamp.conf.getKeys(false)
        val players = mutableListOf<Player>()
        for (uuid in keys) {
            val player = XCorePlugin.instance.server.getPlayer(UUID.fromString(uuid)) ?: continue
            val stamp = HashSet(getEntireStampList())
            stamp.removeAll(activatedStamp.conf.getStringList(uuid).toSet())
            if (stamp.isEmpty()) players.add(player)
        }
        return players
    }

    @JvmStatic
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
            EbipowerModule.tryGive(player, 5000)
        }
    }

    @JvmStatic
    fun isStampAchieved(player: Player): Boolean {
        val entireStamp = HashSet(getEntireStampList())
        val activated = activatedStamp.conf.getStringList(player.uniqueId.toString())
        entireStamp.removeAll(activated.toSet())
        return entireStamp.isEmpty()
    }

    @JvmStatic
    fun getEntireStampList(): List<String> {
        return stampList.conf.getStringList("stamps")
    }

    @JvmStatic
    fun getActivatedStampList(player: Player): List<String> {
        return activatedStamp.conf.getStringList(player.uniqueId.toString())
    }

    @JvmStatic
    fun getStampInfo(stampName: String): Stamp {
        val data = stampList.conf.get(stampName)
        return Stamp.deserialize(data as MemorySection)
    }

    @JvmStatic
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

    @JvmStatic
    fun contains(stampName: String): Boolean {
        val stamps = getEntireStampList()
        return stamps.contains(stampName)
    }

    @JvmStatic
    fun destroy(stampName: String) {
        if (!contains(stampName)) return
        val stamps = stampList.conf.getStringList("stamps")
        stamps.remove(stampName)
        stampList.conf.set("stamps", stamps)
        stampList.conf.set(stampName, null)
        stampList.save()
    }
}