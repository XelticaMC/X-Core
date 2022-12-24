package work.xeltica.craft.core.modules.stamprally

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.utils.EventUtility
import work.xeltica.craft.core.xphone.apps.AppBase
import java.util.function.Consumer

class StampRallyApp: AppBase() {
    override fun getName(player: Player): String = "スタンプラリー"

    override fun getIcon(player: Player): Material {
        return Material.TARGET
    }

    override fun onLaunch(player: Player) {
        val entireStamp = StampRallyModule.getEntireStampList()
        val hasStamp = StampRallyModule.getActivatedStampList(player)

        val ui = Gui.getInstance()
        val list = mutableListOf<MenuItem>()
        val icon = fun (stampName: String): Material {
            return if (hasStamp.contains(stampName)) Material.LIME_DYE else Material.GRAY_DYE
        }

        val onClick = Consumer<MenuItem> {
            val stampInfo = StampRallyModule.getStampInfo(it.name)

            val sb = StringBuilder()
            sb.appendLine("ワールド：" + WorldModule.getWorldDisplayName(stampInfo.loc.world.name))
            sb.append("座標：(")
            sb.append(stampInfo.loc.blockX)
            sb.append(", ")
            sb.append(stampInfo.loc.blockY)
            sb.append(", ")
            sb.append(stampInfo.loc.blockZ)
            sb.appendLine(")")
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