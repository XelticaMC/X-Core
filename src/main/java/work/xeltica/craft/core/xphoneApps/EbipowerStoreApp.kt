package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * エビパワーストアアプリ。
 * @author Ebise Lutica
 */
class EbipowerStoreApp : AppBase() {
    override fun getName(player: Player) = "エビパワーストア"

    override fun getIcon(player: Player) = Material.HEART_OF_THE_SEA

    override fun onLaunch(player: Player) {
        player.performCommand("epshop")
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

