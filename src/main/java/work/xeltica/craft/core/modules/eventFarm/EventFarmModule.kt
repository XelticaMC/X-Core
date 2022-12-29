package work.xeltica.craft.core.modules.eventFarm

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.XCoreException
import work.xeltica.craft.core.modules.nbs.NbsModel
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.utils.Ticks
import java.util.*

object EventFarmModule : ModuleBase() {
    override fun onEnable() {
        init()
        registerCommand("farmfest", FarmfestCommand())
        registerHandler(EventFarmHandler())

        val world = Bukkit.getWorld("event")
        locations = listOf(
            Location(world, 66.0, 133.0, 911.0),
            Location(world, 66.0, 133.0, 977.0),
            Location(world, 0.0, 133.0, 911.0),
            Location(world, 0.0, 133.0, 977.0),
        )
    }

    fun init() {
        board.clear()
    }

    @Throws(XCoreException::class)
    fun addPlayerToBoard(player: Player) {
        if (board.containsKey(player)) {
            throw XCoreException("${player.name} を追加しようとしましたが、既に存在します。")
        }
        if (board.keys.size >= 4) {
            throw XCoreException("${player.name} を追加しようとしましたが、定員オーバーです。")
        }
        board[player] = 0
        player.sendMessage("祭りの参加メンバーに追加されました。開始まで、今しばらくお待ちください。")
    }

    fun clearFarm() {
        val world = Bukkit.getWorld("event")
        for (z in (MAP_TOP..MAP_BOTTOM)) {
            for (x in (MAP_LEFT..MAP_RIGHT)) {
                val loc = Location(world, x.toDouble(), 133.0, z.toDouble())
                val lowerBlock = loc.clone().subtract(0.0, 1.0, 0.0).block
                if (lowerBlock.type != Material.FARMLAND) continue
                loc.block.type = if (random.nextInt(100) < 5) Material.WHEAT else Material.POTATOES
                val data = loc.block.blockData
                if (data is Ageable) {
                    data.age = data.maximumAge
                    loc.block.blockData = data
                }
            }
        }
    }

    @Throws(XCoreException::class)
    fun start() {
        if (board.keys.size <= 1) throw XCoreException("人数が集まっていません！")
        isPlaying = true
        countdown = 11
        time = 60 * 3
        clearFarm()
        val players = board.keys.toTypedArray()
        for (i in 0 until board.keys.size) {
            val player = players[i]
            player.teleport(locations[i], PlayerTeleportEvent.TeleportCause.PLUGIN)
            player.gameMode = GameMode.SURVIVAL
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Ticks.from(999.0), 4, false, false, false))
            player.inventory.clear()
        }
        object : BukkitRunnable() {
            override fun run() {
                val eventPlayers = getEventPlayers()
                countdown--
                if (countdown == 0) {
                    this.cancel()
                    eventPlayers.forEach {
                        it.sendTitle("${ChatColor.GREEN}始めッ！", "", 0, 80, 0)
                        it.playSound(it.location, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.PLAYERS, 1f, 1f)
                    }
                    object : BukkitRunnable() {
                        override fun run() {
                            if (!isPlaying) return
                            eventPlayers.forEach {
                                NbsModule.playRadio(it, "farmfest22", NbsModel.PlaybackMode.LOOP)
                            }
                            radioPlayers = eventPlayers
                        }
                    }.runTaskLater(XCorePlugin.instance, Ticks.from(5.0).toLong())
                } else {
                    eventPlayers.forEach {
                        it.sendTitle(countdown.toString(), "", 0, 20, 0)
                        it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.6f)
                    }
                }
            }
        }.runTaskTimer(XCorePlugin.instance, 0, Ticks.from(1.0).toLong())
        object : BukkitRunnable() {
            override fun run() {
                if (!isPlaying) {
                    this.cancel()
                    return
                }
                val eventPlayers = getEventPlayers()
                board.keys.forEach(EventFarmModule::showStatus)
                if (countdown > 0) return
                time--
                if (time == 60) {
                    eventPlayers.forEach {
                        it.sendTitle("あと60秒！", "", 0, 80, 0)
                        it.sendMessage("あと60秒！")
                        it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
                    }
                }
                if (time == 30) {
                    eventPlayers.forEach {
                        it.sendTitle("あと30秒！", "", 0, 80, 0)
                        it.sendMessage("あと30秒！")
                        it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
                    }
                }
                if (time <= 0) {
                    stop()
                } else if (time <= 10) {
                    eventPlayers.forEach {
                        it.sendTitle(time.toString(), "", 0, 20, 0)
                        it.sendMessage(time.toString())
                        it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
                    }
                }
            }
        }.runTaskTimer(XCorePlugin.instance, 0, Ticks.from(1.0).toLong())
    }

    fun stop() {
        isPlaying = false
        getEventPlayers().forEach {
            it.sendTitle("${ChatColor.RED}終わりッ！", "", 0, 80, 0)
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1f, 1f)
        }
        board.keys.forEach {
            it.gameMode = GameMode.ADVENTURE
            it.removePotionEffect(PotionEffectType.SPEED)
            Bukkit.getLogger().info("${it.name}: ${board[it]}ポイント")
        }
        radioPlayers.forEach {
            NbsModule.stopRadio(it)
        }
    }

    fun showStatus(player: Player) {
        val m = time / 60
        val s = time % 60
        player.sendActionBar(Component.text("残り${if (m > 0) "${m}分" else ""}${s}秒 ${ChatColor.BOLD}現在の得点：${ChatColor.RESET}${board[player]}点"))
    }

    fun getEventPlayers() = Bukkit.getOnlinePlayers().filter {
        it.world.name == "event"
    }

    var isPlaying = false
        private set

    var countdown = 0
        private set

    var time = 0
        private set

    val board = mutableMapOf<Player, Int>()

    var radioPlayers = listOf<Player>()

    private val random = Random()

    private lateinit var locations: List<Location>

    private const val MAP_LEFT = 2
    private const val MAP_RIGHT = 64
    private const val MAP_TOP = 913
    private const val MAP_BOTTOM = 975
}