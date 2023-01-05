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
 * プレイヤーが遊ぶ上での足掛かりとなるヒント機能を提供するモジュールです。
 * @author Lutica
 */
object HintModule : ModuleBase() {
    private lateinit var hints: Config

    override fun onEnable() {
        hints = Config("hints")

        registerCommand("hint", HintCommand())
        registerHandler(HintHandler())
    }

    /**
     * [player] が達成したヒントの名前リストを取得します。
     */
    fun getAllAchievedHintNames(player: Player): MutableList<String> {
        return getAllAchievedHintNames(player.uniqueId)
    }

    /**
     * [player] のヒント達成履歴を消去します。
     */
    fun clearHints(player: Player) {
        hints.conf[player.uniqueId.toString()] = null
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player] が [hint] を達成済みかどうかを取得します。
     */
    fun hasAchieved(player: Player, hint: Hint): Boolean {
        return hint.name in getAllAchievedHintNames(player)
    }

    /**
     * [player] が [hint] を達成します。[reward] がtrueの場合、リワードを与えます。
     */
    fun achieve(player: Player, hint: Hint, reward: Boolean = true) {
        if (hasAchieved(player, hint)) return
        val list = getAllAchievedHintNames(player)
        list.add(hint.name)
        hints.conf[player.uniqueId.toString()] = list
        if (reward) {
            giveReward(player, hint)
        }
        try {
            save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getAllAchievedHintNames(id: UUID): MutableList<String> {
        return hints.conf.getStringList(id.toString())
    }

    private fun giveReward(player: Player, hint: Hint) {
        if (hint.power > 0) {
            EbiPowerModule.tryGive(player, hint.power)
        }
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1.4f)

        val hintNameComponent = Component.text(hint.hintName).color(TextColor.color(0x03A9F4))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(hint.description)))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/hint " + hint.name))

        val component = player.displayName().color(TextColor.color(0x4CAF50))
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
    }

    @Throws(IOException::class)
    fun save() {
        hints.save()
    }
}