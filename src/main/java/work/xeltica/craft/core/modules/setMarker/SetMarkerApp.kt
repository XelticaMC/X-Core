package work.xeltica.craft.core.modules.setMarker

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.xphone.AppBase

class SetMarkerApp : AppBase() {
    override fun getName(player: Player): String = "座標登録"

    override fun getIcon(player: Player): Material = Material.TNT

    override fun onLaunch(player: Player) {
        val gui = Gui.getInstance()

        gui.openMenu(player, "マーカーメニュー", listOf(
                MenuItem("新規設置", {
                    SetMarkerModule.infoMarker(player)
                    SetMarkerModule.searchLocationPid(player.location, player.world.name)
                }, Material.REDSTONE_TORCH),
                MenuItem("移動", {
                    SetMarkerModule.infoMarker(player)
                    SetMarkerModule.searchLocationPid(player.location, player.world.name)
                }, Material.SOUL_TORCH),
                MenuItem("全消去", {
                    SetMarkerModule.dellAll(player)
                }, Material.STRUCTURE_VOID),
                MenuItem("確認", {
                    SetMarkerModule.infoMarker(player)
                    SetMarkerModule.searchLocationPid(player.location, player.world.name)
                }, Material.KNOWLEDGE_BOOK),
                MenuItem("ツール取得", {
                    player.world.dropItem(player.location, SetMarkerModule.createMarkerToolAD(1))
                }, Material.CARROT_ON_A_STICK),
        ))
    }
}