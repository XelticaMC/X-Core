package work.xeltica.craft.core.stores

import org.bukkit.Location
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.Stamp
import work.xeltica.craft.core.utils.Config

class StampRallyStore {
    companion object {
        private lateinit var instance: StampRallyStore
        fun getInstance(): StampRallyStore = instance

        const val CREATE_PERMISSION = "otanoshimi.stamp.create"
        const val DESTROY_PERMISSION = "otanoshimi.stamp.destroy"
    }

    private val stampList: Config
    private val activatedStamp: Config

    init {
        instance = this
        stampList = Config("stampList")
        activatedStamp = Config("activatedStamp")
    }

    fun activate(player: Player, stampName: String) {
        val activated = activatedStamp.conf.getStringList(player.uniqueId.toString())

        if (activated.contains(stampName)) {
            player.sendMessage(stampName + "はもう押されています！")
            return
        }

        player.sendMessage(stampName + "のスタンプを押しました!")
        val list = mutableListOf<String>()
        list.addAll(activated)
        list.add(stampName)
        activatedStamp.conf.set(player.uniqueId.toString(), list)
        activatedStamp.save()
    }

    fun getEntireStampList(): List<String> {
        return stampList.conf.getStringList("stamps")
    }

    fun getActivatedStampList(player: Player): List<String> {
        return activatedStamp.conf.getStringList(player.uniqueId.toString())
    }

    fun getStampInfo(stampName: String): Stamp {
        val data = stampList.conf.get(stampName)
        return Stamp.deserialize(data as MemorySection)
    }

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

    fun contains(stampName: String): Boolean {
        val stamps = getEntireStampList()
        return stamps.contains(stampName)
    }

    fun destroy(stampName: String) {
        if (!contains(stampName)) return
        val stamps = stampList.conf.getStringList("stamps")
        stamps.remove(stampName)
        stampList.conf.set("stamps", stamps)
        stampList.conf.set(stampName, null)
        stampList.save()
    }
}