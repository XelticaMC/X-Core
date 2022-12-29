package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.player.PlayerDataKey

/**
 * ネコモードを切り替えるプラグイン。
 * @author Ebise Lutica
 */
class CatApp : AppBase() {
    override fun getName(player: Player): String {
        return "ネコ語モードを${if (isCat(player)) "オフ" else "オン"}にする"
    }

    override fun getIcon(player: Player): Material = Material.COD

    override fun onLaunch(player: Player) {
        player.performCommand("cat ${if (isCat(player)) "off" else "on"}")
    }

    override fun isShiny(player: Player): Boolean = isCat(player)

    private fun isCat(player: Player) = PlayerStore.open(player).getBoolean(PlayerDataKey.CAT_MODE)
}
