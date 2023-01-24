package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * 乗換案内アプリ
 * @author Knit prg.
 */
class TransferGuideApp : AppBase() {
    override fun getName(player: Player): String = "乗換案内"

    override fun getIcon(player: Player): Material = Material.POWERED_RAIL

    override fun onLaunch(player: Player) {
        TransferGuideSession(player).start()
    }

    override fun isVisible(player: Player): Boolean {
        return try {
            TransferGuideData().availableWorlds.contains(player.world.name)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("路線データが破損しています！")
            e.printStackTrace()
            false
        }
    }
}