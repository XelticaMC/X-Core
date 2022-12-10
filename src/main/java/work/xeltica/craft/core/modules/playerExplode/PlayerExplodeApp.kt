package work.xeltica.craft.core.modules.playerExplode

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class PlayerExplodeApp : AppBase() {
    override fun getName(player: Player): String = "汚え花火"

    override fun getIcon(player: Player): Material = Material.FIREWORK_STAR

    override fun onLaunch(player: Player) {
        player.world.createExplosion(player.location, 5f)
    }
}
