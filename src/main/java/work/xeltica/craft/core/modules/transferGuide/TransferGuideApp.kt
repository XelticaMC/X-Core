package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class TransferGuideApp : AppBase() {
    override fun getName(player: Player): String = "Knit乗換案内(デバッグ中)"

    override fun getIcon(player: Player): Material = Material.POWERED_RAIL

    override fun onLaunch(player: Player) {
        val session = TransferGuideSession(player)
        session.start()
    }
}