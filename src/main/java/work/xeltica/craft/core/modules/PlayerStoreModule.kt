package work.xeltica.craft.core.modules

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.ItemMeta
import work.xeltica.craft.core.api.Ticks.from
import work.xeltica.craft.core.modules.BossBarModule.add
import work.xeltica.craft.core.modules.BossBarModule.remove
import work.xeltica.craft.core.models.PlayerRecord
import kotlin.Throws
import java.io.IOException
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import java.lang.Runnable
import java.util.*

/**
 * プレイヤー固有データの管理を行い、読み書きを行うPlayer Store APIを提供します。
 * @author Xeltica
 */
object PlayerStoreModule : ModuleBase() {
    override fun onEnable() {
        playerStores = Config("playerStores")
        Bukkit.getScheduler().runTaskTimer(XCorePlugin.instance, Runnable { saveTask() }, 0, from(10.0).toLong())
    }

    override fun onDisable() {
        liveBarMap.clear()
        super.onDisable()
    }

    @JvmStatic
    fun open(p: OfflinePlayer): PlayerRecord {
        return open(p.uniqueId)
    }

    @JvmStatic
    fun open(id: UUID): PlayerRecord {
        var section = playerStores.conf.getConfigurationSection(id.toString())
        if (section == null) {
            section = playerStores.conf.createSection(id.toString())
        }
        return PlayerRecord(playerStores, section, id)
    }

    @JvmStatic
    fun openAll(): List<PlayerRecord> {
        return playerStores
            .conf
            .getKeys(false)
            .stream()
            .map { k: String? -> open(UUID.fromString(k)) }
            .toList()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun save() {
        playerStores.save()
    }

    @JvmStatic
    fun isCitizen(p: Player): Boolean {
        return p.hasPermission("otanoshimi.citizen")
    }

    @JvmStatic
    fun setLiveMode(player: Player, isLive: Boolean) {
        if (isLive == isLiveMode(player)) return
        if (isLive) {
            val name = String.format("%s が配信中", player.name)
            val bar =
                BossBar.bossBar(Component.text(name), BossBar.MAX_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
            liveBarMap[player.uniqueId] = bar
            add(bar)
        } else {
            val bar = liveBarMap[player.uniqueId]
            liveBarMap.remove(player.uniqueId)
            remove(bar!!)
        }
    }

    @JvmStatic
    fun isLiveMode(p: Player): Boolean {
        return liveBarMap.containsKey(p.uniqueId)
    }

    @JvmStatic
    fun getRandomFireworkByUUID(id: UUID, amount: Int): ItemStack {
        val random = Random(id.hashCode().toLong())
        val item = ItemStack(Material.FIREWORK_ROCKET, amount)
        item.editMeta { meta: ItemMeta ->
            val firework = meta as FireworkMeta
            val effect = FireworkEffect.builder()
                .trail(random.nextBoolean())
                .flicker(random.nextBoolean())
                .with(FireworkEffect.Type.values()[random.nextInt(5)])
                .withColor(colors[random.nextInt(colors.size)])
                .build()
            firework.addEffect(effect)
            firework.power = 1
        }
        return item
    }

    @JvmStatic
    fun isOnline(player: Player): Boolean {
        return isOnline(player.uniqueId)
    }

    @JvmStatic
    fun isOnline(id: UUID): Boolean {
        return Bukkit.getOnlinePlayers().stream().anyMatch { p: Player -> p.uniqueId == id }
    }

    @JvmStatic
    fun getliveBarMap(): Map<UUID, BossBar>? {
        return liveBarMap
    }

    private fun saveTask() {
        if (!isChanged) return
        try {
            save()
            isChanged = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    var isChanged = false

    private lateinit var playerStores: Config
    private val liveBarMap = mutableMapOf<UUID, BossBar>()
    private val colors = arrayOf( // from XelticaUI
        Color.fromRGB(0xe23731),  // red
        Color.fromRGB(0xeb6101),  // vermilion
        Color.fromRGB(0xf08300),  // orange
        Color.fromRGB(0xe9be00),  // yellow
        Color.fromRGB(0xb8d200),  // lime
        Color.fromRGB(0x3eb370),  // green
        Color.fromRGB(0x20c0a0),  // teal
        Color.fromRGB(0x43fcf3),  // cyan
        Color.fromRGB(0x00b7ff),  // skyblue
        Color.fromRGB(0x2571ff),  // blue
        Color.fromRGB(0xff55a1),  // magenta
        Color.fromRGB(0xff5c84)
    )
}