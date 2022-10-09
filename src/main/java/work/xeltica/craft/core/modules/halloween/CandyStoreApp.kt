package work.xeltica.craft.core.modules.halloween

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class CandyStoreApp : AppBase() {
    override fun getName(player: Player): String = "アメストア"

    override fun getIcon(player: Player): Material = Material.HEART_OF_THE_SEA

    override fun onLaunch(player: Player) {
        HalloweenModule.openCandyStore(player)
    }

    override fun isVisible(player: Player): Boolean {
        if (player.world.name == "event2") return true
        // TODO イベント開始フラグなどを反映する

        return false
    }

    override fun isShiny(player: Player): Boolean = true
}