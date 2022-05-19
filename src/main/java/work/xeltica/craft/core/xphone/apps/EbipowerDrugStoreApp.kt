package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * エビパワードラッグストアアプリ。
 * @author Ebise Lutica
 */
class EbipowerDrugStoreApp : AppBase() {
    override fun getName(player: Player): String = "エビパワードラッグストア"

    override fun getIcon(player: Player): Material = Material.BREWING_STAND

    override fun onLaunch(player: Player) {
        player.performCommand("epeffectshop")
    }
}

