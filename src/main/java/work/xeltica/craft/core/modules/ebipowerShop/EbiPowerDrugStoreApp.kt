package work.xeltica.craft.core.modules.ebipowerShop

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * エビパワードラッグストアアプリ。
 * @author Lutica
 */
class EbiPowerDrugStoreApp : AppBase() {
    override fun getName(player: Player): String = "エビパワードラッグストア"

    override fun getIcon(player: Player): Material = Material.BREWING_STAND

    override fun onLaunch(player: Player) {
        player.performCommand("epeffectshop")
    }

    override fun isVisible(player: Player) = listOf(
        "main",
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "wildareab",
        "nightmare2",
    ).contains(player.world.name)
}

