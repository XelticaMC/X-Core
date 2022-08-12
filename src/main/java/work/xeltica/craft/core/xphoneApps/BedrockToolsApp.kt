package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.MenuItem
import work.xeltica.craft.core.modules.XphoneModule
import work.xeltica.craft.core.modules.BedrockDisclaimerModule

/**
 * 統合版ユーザー向けツールを揃えたアプリ。
 * @author Ebise Lutica
 */
class BedrockToolsApp : AppBase() {
    override fun getName(player: Player): String = "統合版ツール"

    override fun getIcon(player: Player): Material = Material.BEDROCK

    override fun onLaunch(player: Player) {
        Gui.getInstance().openMenu(player, "保護方法を選択…", listOf(
            MenuItem(
                "進捗",
                { player.performCommand("geyser advancements") },
                Material.BEDROCK,
                null
            ),
            MenuItem(
                "統計",
                { player.performCommand("geyser statistics") },
                Material.BEDROCK,
                null
            ),
            MenuItem(
                "免責事項",
                { BedrockDisclaimerModule.showDisclaimer(player) },
                Material.BEDROCK,
                null
            ),
        ))
    }

    override fun isVisible(player: Player) = XphoneModule.isBedrockPlayer(player)
}
