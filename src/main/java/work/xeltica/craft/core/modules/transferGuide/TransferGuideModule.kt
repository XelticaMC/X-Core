package work.xeltica.craft.core.modules.transferGuide

import java.time.LocalDateTime
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config

object TransferGuideModule : ModuleBase() {
    override fun onEnable() {
        updateData()
        TransferGuideSession.verifyData()
        Bukkit.getLogger().info("[TransferGuide] Knit乗換案内を読み込みまいた。")
    }

    private fun willBeUpdated(): Boolean {
        val resource = XCorePlugin.instance.getResource("transferGuideData.yml") ?: return false
        val confFromResourceUpdatedStr =
            YamlConfiguration.loadConfiguration(resource.reader()).getString("update") ?: return false
        val confFromResourceUpdated = LocalDateTime.parse(confFromResourceUpdatedStr) ?: return false
        val confFromPluginFolderUpdatedStr = Config("transferGuideData").conf.getString("update") ?: return true
        val confFromPluginFolderUpdated = LocalDateTime.parse(confFromPluginFolderUpdatedStr) ?: return false
        return confFromResourceUpdated.isAfter(confFromPluginFolderUpdated)
    }

    private fun updateData(): Boolean {
        val willBeUpdated = willBeUpdated()
        if (willBeUpdated) {
            XCorePlugin.instance.saveResource("transferGuideData.yml", true)
            Bukkit.getLogger().info("[TransferGuide] XCore.jar内の路線データの方が新しいため、plugins/XCore内の路線データを上書きしました。")
        }
        return willBeUpdated
    }
}