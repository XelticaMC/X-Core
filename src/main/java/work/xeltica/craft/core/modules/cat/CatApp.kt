package work.xeltica.craft.core.modules.cat

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.cat.CatModule.isCat
import work.xeltica.craft.core.modules.cat.CatModule.setCat
import work.xeltica.craft.core.xphone.apps.AppBase

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
        setCat(player, !isCat(player))
    }

    override fun isShiny(player: Player): Boolean = isCat(player)
}
