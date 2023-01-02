package work.xeltica.craft.core.modules.eventFirework

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.utils.Ticks
import java.util.Random

object EventFireworkModule : ModuleBase() {
    lateinit var scripts: Map<String, List<OperationBase>>
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

        var scriptsSection = config.conf.getConfigurationSection(CONFIG_KEY_SCRIPTS)
        if (scriptsSection == null) {
            XCorePlugin.instance.saveResource("${CONFIG_NAME}.yml", false)
            config.reload()
            scriptsSection = config.conf.getConfigurationSection(CONFIG_KEY_SCRIPTS) ?: return
        }
        val scriptNames = scriptsSection.getKeys(false)
        val map = mutableMapOf<String, List<OperationBase>>()
        scriptNames.forEach { name ->
            val commandMaps = scriptsSection.getMapList(name)
            val commands = commandMaps.map {
                when (val commandType = it["command"]) {
                    "fire" -> {
                        assertProperty(it, "type", "fire")
                        val typeString = it["type"] as String
                        val type = FireworkEffect.Type.valueOf(typeString)
                        val colors = tryCast(it["colors"], listOf("RANDOM"))
                        val fades = tryCast(it["fades"], listOf<String>())
                        val flicker = tryCast(it["flicker"], false)
                        val trail = tryCast(it["trail"], false)
                        val loc = tryCast(it["loc"], listOf(0.0, 0.0, 0.0))
                        val random = tryCast(it["random"], 0)
                        val clone = tryCast(it["clone"], 0)
                        val power = tryCast(it["power"], 1)
                        FireOperation(type, colors, fades, flicker, trail, loc, random, clone, power)
                    }

                    "wait" -> {
                        assertProperty(it, "time", "wait")
                        WaitOperation(it["time"] as Double)
                    }

                    "explode" -> {
                        ExplodeOperation()
                    }

                    else -> {
                        throw IllegalStateException("$commandType is not a valid command type.")
                    }
                }
            }
            map[name] = commands
        }
        scripts = map
    }

    fun runScript(script: List<OperationBase>, sender: CommandSender? = null) {
        fun log(text: String) {
            sender?.sendMessage(text)
        }

        val c = center?.clone() ?: return
        object : BukkitRunnable() {
            // スクリプトの現在位置
            var index = 0
            var waitTimer = 0

            override fun run() {
                if (waitTimer > 0) {
                    waitTimer--
                    return
                }
                while (index < script.size) {
                    val command = script[index]
                    when (command) {
                        is FireOperation -> {
                            repeat(command.clone + 1) {
                                val fireLoc = c.clone()
                                    .add(command.loc[0], command.loc[1], command.loc[2])
                                    .add(random.nextDouble(-1.0, 1.0) * command.random, 0.0, random.nextDouble(-1.0, 1.0) * command.random)
                                val color = command.colors.map {
                                    if (it == "RANDOM") {
                                        val colorNames = colorsMap.values.toList()
                                        colorNames[random.nextInt(colorNames.size)]
                                    } else {
                                        colorsMap[it] ?: Color.RED
                                    }
                                }
                                val fades = command.fades.map {
                                    if (it == "RANDOM") {
                                        val colorNames = colorsMap.values.toList()
                                        colorNames[random.nextInt(colorNames.size)]
                                    } else {
                                        colorsMap[it] ?: Color.RED
                                    }
                                }
                                log("* Creating ${command.type} firework at ${fireLoc.x}, ${fireLoc.y}, ${fireLoc.z}.")
                                log("* * Color: [${command.colors.joinToString(", ")}]")
                                log("* * Fade: [${command.fades.joinToString(", ")}]")
                                log("* * Flicker: ${command.flicker}")
                                log("* * Trail: ${command.trail}")
                                log("* * Power: ${command.power}")
                                val firework = FireworkEffect.builder()
                                    .with(command.type)
                                    .withColor(color)
                                    .flicker(command.flicker)
                                    .trail(command.trail)
                                if (fades.isNotEmpty()) {
                                    firework.withFade(fades)
                                }

                                fireLoc.world.spawnEntity(fireLoc, EntityType.FIREWORK, SpawnReason.CUSTOM) {
                                    it as Firework
                                    val meta = it.fireworkMeta
                                    meta.power = command.power
                                    meta.addEffect(firework.build())
                                    it.fireworkMeta = meta
                                }
                            }
                        }

                        is WaitOperation -> {
                            log("* Waiting for ${command.time}s.")
                            index++
                            waitTimer = Ticks.from(command.time)
                            return
                        }

                        is ExplodeOperation -> {
                            repeat(command.clone + 1) {
                                val fireLoc = c.clone()
                                    .add(command.loc[0], command.loc[1], command.loc[2])
                                    .add(random.nextDouble(-1.0, 1.0) * command.random, 0.0, random.nextDouble(-1.0, 1.0) * command.random)
                                log("* Creating explosion at ${fireLoc.x}, ${fireLoc.y}, ${fireLoc.z}.")
                                fireLoc.world.createExplosion(fireLoc, 0f, false, false)
                            }
                        }
                    }
                    index++
                }
                this.cancel()
            }
        }.runTaskTimer(XCorePlugin.instance, 0L, 1L)
    }

    fun setCenterLocation(centerLocation: Location) {
        center = centerLocation
        config.conf.set(CONFIG_KEY_CENTER_LOCATION, centerLocation)
        config.save()
    }

    private fun assertProperty(map: Map<*, *>, prop: String, command: String) {
        if (!map.containsKey(prop)) {
            throw IllegalStateException("property '$prop' is required for '$command' command.")
        }
    }

    private inline fun <reified T> tryCast(value: Any?, defaultValue: T): T {
        return if (value == null) defaultValue else if (value is T) value else throw IllegalStateException("The value type is invalid.")
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