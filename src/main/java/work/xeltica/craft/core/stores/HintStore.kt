package work.xeltica.craft.core.stores

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.Hint
import java.io.IOException
import work.xeltica.craft.core.services.DiscordService
import java.util.UUID
import kotlin.Throws
import work.xeltica.craft.core.api.Ticks.Config

/**
 * プレイヤーのヒントを達成する処理や、ヒントを達成しているかどうかの取得などを行います。
 * @author Xeltica
 */
class HintStore {
    fun getArchived(p: Player): List<String> {
        return open(p)
    }

    fun deleteArchiveData(p: Player) {
        hints.conf[p.uniqueId.toString()] = null
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun hasAchieved(p: Player, hint: Hint): Boolean {
        return open(p).contains(hint.name)
    }

    fun achieve(p: Player, hint: Hint, reward: Boolean) {
        if (reward) {
            achieve(p, hint)
            return
        }
        if (hasAchieved(p, hint)) return
        val list = open(p)
        list.add(hint.name)
        hints.conf[p.uniqueId.toString()] = list
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun achieve(p: Player, hint: Hint) {
        if (hasAchieved(p, hint)) return
        val list = open(p)
        list.add(hint.name)
        hints.conf[p.uniqueId.toString()] = list
        if (hint.power > 0) {
            EbiPowerStore.getInstance().tryGive(p, hint.power)
        }
        p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1.4f)
        val component = p.displayName().color(TextColor.color(0x4CAF50))
            .append(Component.text("さんがヒント「"))
            .append(Component
                .text(hint.hintName)
                .color(TextColor.color(0x03A9F4))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(hint.description)))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/hint " + hint.name)))
            .append(Component.text("」を達成しました！"))
            .asComponent()
        DiscordService.getInstance().broadcast(PlainTextComponentSerializer.plainText().serialize(component))
        Bukkit.getServer().audiences().forEach { a: Audience? ->
            a!!.sendMessage(component)
            if (hint.type === Hint.HintType.CHALLENGE) {
                a.playSound(
                    net.kyori.adventure.sound.Sound.sound(
                        Key.key("ui.toast.challenge_complete"),
                        net.kyori.adventure.sound.Sound.Source.PLAYER,
                        1f,
                        1f
                    )
                )
            }
        }
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun open(p: Player): MutableList<String> {
        return open(p.uniqueId)
    }

    private fun open(id: UUID): MutableList<String> {
        return hints.conf.getStringList(id.toString())
    }

    @Throws(IOException::class)
    fun save() {
        hints.save()
    }

    private val hints: Config

    init {
        instance = this
        hints = Config("hints")
    }

    companion object {
        @JvmStatic
        lateinit var instance: HintStore
            private set
    }
}