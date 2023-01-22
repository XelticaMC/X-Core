package work.xeltica.craft.core.modules.setMarker

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.xphone.AppBase

class SetMarkerApp : AppBase() {
    override fun getName(player: Player): String = "座標登録"

    override fun getIcon(player: Player): Material = Material.TNT

    override fun onLaunch(player: Player) {
        // TODO("Not yet implemented")
        val gui = Gui.getInstance()

        gui.openMenu(player, "test", listOf(
                MenuItem("マーカー設置", {
                    SetMarkerModule.setMarker(player)
                }, Material.REDSTONE_TORCH),
                MenuItem("確認", {
                    SetMarkerModule.infoMarker(player)
                    Bukkit.getLogger().info(SetMarkerModule.getAllPrefix().toString())
                }, Material.BLUE_DYE),
                MenuItem("削除", {
                    SetMarkerModule.dellAll(player)
                }, Material.RED_DYE),
                MenuItem("ツール取得", {
                    player.world.dropItem(player.location, SetMarkerModule.createMarkerToolAD(1))
                }, Material.SOUL_TORCH),
        ))
    }
}