package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * エビパワーストアアプリ。
 * @author Ebise Lutica
 */
class EbipowerStoreApp : AppBase() {
    override fun getName(player: Player): String = "エビパワーストア"

    override fun getIcon(player: Player): Material = Material.HEART_OF_THE_SEA

    override fun onLaunch(player: Player) {
        player.performCommand("epshop")
    }
}

