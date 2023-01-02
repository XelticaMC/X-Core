package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem

/**
 * インベントリを保護するためのアプリ
 * @author Ebise Lutica
 */
class ProtectApp : AppBase() {
    override fun getName(player: Player): String = "インベントリ保護"

    override fun getIcon(player: Player): Material = Material.TRIPWIRE_HOOK

    override fun onLaunch(player: Player) {
        Gui.getInstance().openMenu(
            player, "保護方法を選択…", listOf(
                MenuItem("個人保護", { player.performCommand("cprivate") }, Material.RED_DYE, null),
                MenuItem("共有保護", { player.performCommand("cpublic") }, Material.GREEN_DYE, null),
                MenuItem("保護を削除する", { player.performCommand("cremove") }, Material.BARRIER, null),
            )
        )
    }

    override fun isVisible(player: Player): Boolean = listOf(
        "main",
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
    ).contains(player.world.name)
}
