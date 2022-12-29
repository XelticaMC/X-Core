package work.xeltica.craft.core.modules.player

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.bossbar.BossBarModule
import work.xeltica.craft.core.utils.Config
import java.util.*
import kotlin.collections.HashMap

object PlayerModule: ModuleBase() {
    private lateinit var config: Config
    val liveBarMap = HashMap<UUID, BossBar>()

    private val colors = listOf(
        Color.fromRGB(0xe23731), // red
        Color.fromRGB(0xeb6101), // vermilion
        Color.fromRGB(0xf08300), // orange
        Color.fromRGB(0xe9be00), // yellow
        Color.fromRGB(0xb8d200), // lime
        Color.fromRGB(0x3eb370), // green
        Color.fromRGB(0x20c0a0), // teal
        Color.fromRGB(0x43fcf3), // cyan
        Color.fromRGB(0x00b7ff), // skyblue
        Color.fromRGB(0x2571ff), // blue
        Color.fromRGB(0xff55a1), // magenta
        Color.fromRGB(0xff5c84), // pink
    )

    override fun onEnable() {
        config = Config("playerStores")
        config.useAutoSave = true

        registerHandler(PlayerHandler())
    }

    fun open(player: OfflinePlayer): PlayerRecord {
        return open(player.uniqueId)
    }

    fun open(uuid: UUID?): PlayerRecord {
        if (uuid == null) throw IllegalArgumentException()
        var section = config.conf.getConfigurationSection(uuid.toString())
        if (section == null) {
            section = config.conf.createSection(uuid.toString())
        }
        return PlayerRecord(config, section, uuid)
    }

    fun openAll(): List<PlayerRecord> {
        return config.conf
            .getKeys(false)
            .map { open(UUID.fromString(it)) }
            .toList()
    }

    fun getRandomFireworkByUUID(uuid: UUID, amount: Int): ItemStack {
        val random = Random(uuid.hashCode().toLong())
        val item = ItemStack(Material.FIREWORK_ROCKET, amount)
        item.editMeta { meta ->
            if (meta !is FireworkMeta) return@editMeta
            val effect = FireworkEffect.builder()
                .trail(random.nextBoolean())
                .flicker(random.nextBoolean())
                .with(FireworkEffect.Type.values()[random.nextInt(5)])
                .withColor(colors[random.nextInt(colors.size)])
                .build()
            meta.addEffect(effect)
            meta.power = 1
        }
        return item
    }

    fun isCitizen(player: Player): Boolean {
        return player.hasPermission("otanoshimi.citizen")
    }

    fun setLiveMode(player: Player, isLive: Boolean) {
        if (isLive == isLiveMode(player)) return
        if (isLive) {
            val name = "%s が配信中".format(player.name)
            val bar = BossBar.bossBar(Component.text(name), BossBar.MAX_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS)

            liveBarMap[player.uniqueId] = bar
            BossBarModule.add(bar)
        } else {
            val bar = liveBarMap[player.uniqueId] ?: return

            liveBarMap.remove(player.uniqueId)
            BossBarModule.remove(bar)
        }
    }

    fun isLiveMode(player: Player): Boolean {
        return liveBarMap.containsKey(player.uniqueId)
    }

    fun isOnline(player: Player): Boolean {
        return isOnline(player.uniqueId)
    }

    fun isOnline(uuid: UUID): Boolean {
        return Bukkit.getOnlinePlayers().any { it.uniqueId == uuid }
    }
}