package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.stores.PlayerStore

/**
 * ネコモードを切り替えるプラグイン。
 * @author Ebise Lutica
 */
class CatApp : AppBase() {
    override fun getName(player: Player): String {
        return "ネコ語モードを${if (isCat(player)) "オフ" else "オン"}にする"
    }

    override fun getIcon(player: Player): Material = Material.GOLD_INGOT

    override fun onLaunch(player: Player) {
        player.performCommand("omikuji")
    }

    override fun isShiny(player: Player): Boolean = isCat(player)

    private fun isCat(player: Player) = PlayerStore.getInstance().open(player).getBoolean(PlayerDataKey.CAT_MODE)
}
