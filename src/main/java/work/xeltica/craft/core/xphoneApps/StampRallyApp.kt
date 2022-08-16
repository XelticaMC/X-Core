package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.MenuItem
import work.xeltica.craft.core.stores.StampRallyStore
import work.xeltica.craft.core.utils.EventUtility
import java.util.function.Consumer

class StampRallyApp: AppBase() {
    override fun getName(player: Player): String = "スタンプラリー"

    override fun getIcon(player: Player): Material {
        return Material.TARGET
    }

    override fun onLaunch(player: Player) {
        val stampRallyStore = StampRallyStore.getInstance()
        val entireStamp = stampRallyStore.getEntireStampList()
        val hasStamp = stampRallyStore.getActivatedStampList(player)

        val ui = Gui.getInstance()
        val list = mutableListOf<MenuItem>()
        val icon = fun (stampName: String): Material {
            return if (hasStamp.contains(stampName)) Material.LIME_DYE else Material.GRAY_DYE
        }

        val onClick = Consumer<MenuItem?> {
            val stampInfo = stampRallyStore.getStampInfo(it.name ?: throw IllegalStateException())
            val sb = StringBuilder()
            sb.append("world: " + stampInfo.loc.world.name + "\n")
            sb.append("x: " + stampInfo.loc.blockX + "\n")
            sb.append("y: " + stampInfo.loc.blockY + "\n")
            sb.append("z: " + stampInfo.loc.blockZ + "\n")
            ui.openDialog(player, stampInfo.name, sb.toString())
        }

        for (stampName in entireStamp) {
            val isShiny = hasStamp.contains(stampName)
            val menuItem = MenuItem(stampName, onClick, icon.invoke(stampName), null, isShiny)
            list.add(menuItem)
        }
        ui.openMenu(player, "スタンプラリー", list)
    }

    override fun isVisible(player: Player): Boolean {
        return EventUtility.isEventNow() || player.isOp
    }
}