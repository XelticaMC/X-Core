package work.xeltica.craft.core.modules.fireworkFestival

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.utils.Config
import work.xeltica.craft.core.utils.Ticks
import java.util.Random

object FireworkFestivalModule : ModuleBase() {
    lateinit var scripts: Map<String, List<FireworkCommandBase>>
        private set

    var center: Location? = null
        private set

    override fun onEnable() {
        reload()
        CommandRegistry.register("firework", FireworkCommand())
    }

    fun reload() {
        config = Config(CONFIG_NAME)

        center = config.conf.getLocation(CONFIG_KEY_CENTER_LOCATION)

        var scripts = config.conf.getConfigurationSection(CONFIG_KEY_SCRIPTS)
        if (scripts == null) {
            XCorePlugin.instance.saveResource("${CONFIG_NAME}.yml", false)
            config.reload()
            scripts = config.conf.getConfigurationSection(CONFIG_KEY_SCRIPTS) ?: return
        }
        val scriptNames = scripts.getKeys(false)
        val map = mutableMapOf<String, List<FireworkCommandBase>>()
        scriptNames.forEach { name ->
            val commandMaps = scripts.getMapList(name)
            val commands = commandMaps.map {
                when (val commandType = it["command"]) {
                    "fire" -> {
                        assertProperty(it, "type", "fire")
                        val typeString = it["type"] as String
                        val colors = it["colors"] as List<String>
                        val type = FireworkEffect.Type.valueOf(typeString)
                        val flicker = it["flicker"] as Boolean
                        val trail = it["trail"] as Boolean
                        val loc = it["loc"] as List<Double>
                        val random = it["random"] as Int
                        val clone = it["clone"] as Int
                        val cloneTick = it["cloneTick"] as Int
                        FireFireworkCommand(type, colors, flicker, trail, loc, random, clone, cloneTick)
                    }
                    "wait" -> {
                        assertProperty(it, "time", "wait")
                        WaitFireworkCommand(it["time"] as Double)
                    }
                    "explode" -> {
                        ExplodeFireworkCommand()
                    }
                    else -> {
                        throw IllegalStateException("$commandType is not a valid command type.")
                    }
                }
            }
            map[name] = commands
        }
    }

    fun runScript(script: List<FireworkCommandBase>) {
        val c = center?.clone() ?: return
        object : BukkitRunnable() {
            // スクリプトの現在位置
            var index = 0
            override fun run() {
                while (index < script.size) {
                    val command = script[index]
                    when (command) {
                        is FireFireworkCommand -> {
                            repeat(command.clone + 1) {
                                val fireLoc = c.clone()
                                    .add(command.loc[0], command.loc[1], command.loc[2])
                                    .add(random.nextDouble(-1.0, 1.0) * command.random, 0.0, random.nextDouble(-1.0, 1.0) * command.random)
                                val color = command.colors.map {
                                    if (it == "RANDOM") {
                                        colorsMap.values.random()
                                    } else {
                                        colorsMap[it] ?: Color.RED
                                    }
                                }
                                val firework = FireworkEffect.builder()
                                    .with(command.type)
                                    .withColor(color)
                                    .flicker(command.flicker)
                                    .trail(command.trail)
                                    .build()
                                fireLoc.world.spawnEntity(fireLoc, EntityType.FIREWORK, SpawnReason.CUSTOM) {
                                    it as Firework
                                    val meta = it.fireworkMeta
                                    meta.power = 2
                                    meta.addEffect(firework)
                                }
                            }
                        }
                        is WaitFireworkCommand -> {
                            runTaskLater(XCorePlugin.instance, Ticks.from(command.time).toLong())
                            index++
                            break
                        }
                        is ExplodeFireworkCommand -> {
                            repeat(command.clone + 1) {
                                val fireLoc = c.clone()
                                    .add(command.loc[0], command.loc[1], command.loc[2])
                                    .add(random.nextDouble(-1.0, 1.0) * command.random, 0.0, random.nextDouble(-1.0, 1.0) * command.random)
                                fireLoc.world.createExplosion(fireLoc, 0f, false, false)
                            }
                        }
                    }
                    index++
                }
            }
        }.runTask(XCorePlugin.instance)
    }

    fun setCenterLocation(centerLocation: Location) {
        center = centerLocation
        config.conf.set(CONFIG_KEY_CENTER_LOCATION, center)
        config.save()
    }

    private fun assertProperty(map: Map<*, *>, prop: String, command: String) {
        if (!map.containsKey(prop)) {
            throw IllegalStateException("property '$prop' is required for '$command' command.")
        }
    }

    private lateinit var config: Config

    private val random: Random = Random()

    private val colorsMap = mapOf(
        "WHITE" to Color.WHITE,
        "SILVER" to Color.SILVER,
        "GRAY" to Color.GRAY,
        "BLACK" to Color.BLACK,
        "RED" to Color.RED,
        "MAROON" to Color.MAROON,
        "YELLOW" to Color.YELLOW,
        "OLIVE" to Color.OLIVE,
        "LIME" to Color.LIME,
        "GREEN" to Color.GREEN,
        "AQUA" to Color.AQUA,
        "TEAL" to Color.TEAL,
        "BLUE" to Color.BLUE,
        "NAVY" to Color.NAVY,
        "FUCHSIA" to Color.FUCHSIA,
        "PURPLE" to Color.PURPLE,
        "ORANGE" to Color.ORANGE,
    )

    private const val CONFIG_NAME = "fireworkFestival"
    private const val CONFIG_KEY_CENTER_LOCATION = "centerLocation"
    private const val CONFIG_KEY_SCRIPTS = "scripts"
}