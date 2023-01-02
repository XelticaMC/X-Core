package work.xeltica.craft.core.modules.cat

import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.SoundPitch
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule

object CatModule : ModuleBase() {
    const val PS_KEY_CAT = "cat"

    override fun onEnable() {
        registerHandler(CatHandler())
        registerCommand("cat", CommandCat())
    }

    fun nyaize(m: String): String {
        var mes = m
        mes = mes.replace("な", "にゃ")
        mes = mes.replace("ナ", "ニャ")
        mes = mes.replace("ﾅ", "ﾆｬ")
        mes = mes.replace("everyone", "everynyan")
        mes = mes.replace("morning", "mornyan")
        mes = mes.replace("na", "nya")
        mes = mes.replace("EVERYONE", "EVERYNYAN")
        mes = mes.replace("MORNING", "MORNYAN")
        mes = mes.replace("NA", "NYA")
        return mes
    }

    fun isCat(player: OfflinePlayer): Boolean {
        return PlayerStore.open(player).getBoolean(PS_KEY_CAT)
    }

    fun setCat(player: Player, value: Boolean) {
        PlayerStore.open(player)[PS_KEY_CAT] = value
        if (value) {
            Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_2)
            player.sendMessage("CATモードを§aオン§rにしました。")
            HintModule.achieve(player, Hint.CAT_MODE)
        } else {
            Gui.getInstance().playSound(player, Sound.ENTITY_CAT_AMBIENT, 2f, SoundPitch.F_0)
            player.sendMessage("CATモードを§cオフ§rにしました。")
        }
    }
}

