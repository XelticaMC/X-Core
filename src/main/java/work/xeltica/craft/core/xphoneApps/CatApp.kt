package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.modules.PlayerStoreModule

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

    private fun isCat(player: Player) = PlayerStoreModule.open(player).getBoolean(PlayerDataKey.CAT_MODE)
}
