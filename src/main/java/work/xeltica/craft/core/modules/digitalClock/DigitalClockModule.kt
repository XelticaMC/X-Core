package work.xeltica.craft.core.modules.digitalClock

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DigitalClockModule : ModuleBase() {
    private val config: Config by lazy { Config("digital-clock") }
    private var holo: Hologram? = null
    private var previousDateString = ""
    private val timePattern = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val datePattern = DateTimeFormatter.ofPattern("yyyy年 MM月 dd日")

    var location: Location? = null
        private set

    override fun onEnable() {
        this.location = config.conf.getLocation("location")
        registerCommand("placedigitalclock", PlaceDigitalClockCommand())
        if (this.location != null) spawn()

        // render関数を毎Tick実行する
        object : BukkitRunnable() {
            override fun run() {
                render()
            }
        }.runTaskTimer(XCorePlugin.instance, 0, 1)
    }

    fun updateLocation(l: Location?) {
        if (location == l) return
        location = l

        config.conf.set("location", l)
        config.save()

        if (l != null) {
            spawn()
        } else {
            despawn()
        }
    }

    fun render() {
        val h = holo ?: return
        val now = LocalDateTime.now()
        val dateString = now.format(timePattern)
        if (dateString == previousDateString) return

        val aa = toAsciiArt(dateString)
        h.clearLines()
        h.appendTextLine(ChatColor.AQUA.toString() + now.format(datePattern))
        for (line in aa) {
            h.appendTextLine(ChatColor.GREEN.toString() + line)
        }
        previousDateString = dateString
    }

    private fun toAsciiArt(text: String): Array<String> {
        val buffer = arrayOf("", "", "", "", "")
        for (c in text) {
            when (c) {
                '0' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "■   ■ "
                    buffer[2] += "■   ■ "
                    buffer[3] += "■   ■ "
                    buffer[4] += "■■■■■ "
                }
                '1' -> {
                    buffer[0] += "    ■ "
                    buffer[1] += "    ■ "
                    buffer[2] += "    ■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "    ■ "
                }
                '2' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "    ■ "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "■     "
                    buffer[4] += "■■■■■ "
                }
                '3' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "    ■ "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "■■■■■ "
                }
                '4' -> {
                    buffer[0] += "■   ■ "
                    buffer[1] += "■   ■ "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "    ■ "
                }
                '5' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "■     "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "■■■■■ "
                }
                '6' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "■     "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "■   ■ "
                    buffer[4] += "■■■■■ "
                }
                '7' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "    ■ "
                    buffer[2] += "    ■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "    ■ "
                }
                '8' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "■   ■ "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "■   ■ "
                    buffer[4] += "■■■■■ "
                }
                '9' -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "■   ■ "
                    buffer[2] += "■■■■■ "
                    buffer[3] += "    ■ "
                    buffer[4] += "■■■■■ "
                }
                ':' -> {
                    buffer[0] += "      "
                    buffer[1] += "  ■   "
                    buffer[2] += "      "
                    buffer[3] += "  ■   "
                    buffer[4] += "      "
                }
                else -> {
                    buffer[0] += "■■■■■ "
                    buffer[1] += "    ■ "
                    buffer[2] += "  ■■ "
                    buffer[3] += "     "
                    buffer[4] += "  ■  "
                }
            }
        }
        return buffer
    }

    private fun spawn() {
        if (location == null) throw Exception("Location is null")
        holo = HologramsAPI.createHologram(XCorePlugin.instance, location)
    }

    private fun despawn() {
        holo?.delete()
        holo = null
    }
}