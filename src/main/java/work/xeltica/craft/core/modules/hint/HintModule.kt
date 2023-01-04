package work.xeltica.craft.core.modules.hint

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
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import java.io.IOException
import java.util.*

/**
 * プレイヤーのヒントを達成する処理や、ヒントを達成しているかどうかの取得などを行います。
 * @author Xeltica
 */
object HintModule : ModuleBase() {
    private lateinit var hints: Config

    override fun onEnable() {
        hints = Config("hints")

        registerCommand("hint", HintCommand())
        registerHandler(HintHandler())
    }

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
            EbiPowerModule.tryGive(p, hint.power)
        }
        p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1.4f)

        val hintNameComponent = Component.text(hint.hintName).color(TextColor.color(0x03A9F4))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(hint.description)))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/hint " + hint.name))

        val component = p.displayName().color(TextColor.color(0x4CAF50))
            .append(Component.text("さんがヒント「"))
            .append(hintNameComponent)
            .append(Component.text("」を達成しました！"))
            .asComponent()

        DiscordHook.broadcast(PlainTextComponentSerializer.plainText().serialize(component))
        Bukkit.getServer().sendMessage(component)
        if (hint.type === Hint.HintType.CHALLENGE) {
            Bukkit.getServer().playSound(
                net.kyori.adventure.sound.Sound.sound(
                    Key.key("ui.toast.challenge_complete"),
                    net.kyori.adventure.sound.Sound.Source.PLAYER,
                    1f,
                    1f
                )
            )
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
}