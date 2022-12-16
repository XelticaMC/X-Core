package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.time.LocalDateTime

object TransferGuideModule : ModuleBase() {
    override fun onEnable() {
        updateData()
        Bukkit.getLogger().info("[TransferGuide] Knit乗換案内を読み込みまいた。")
    }

    private fun willBeUpdated(): Boolean {
        val resource = XCorePlugin.instance.getResource("transferGuideData.yml") ?: run { return false }
        val confFromResourceUpdatedStr =
            resource.reader().let { YamlConfiguration.loadConfiguration(it).getString("update") }
                ?: run { return false }
        val confFromResourceUpdated = LocalDateTime.parse(confFromResourceUpdatedStr) ?: run { return false }
        val confFromPluginFolderUpdatedStr = Config("transferGuideData").conf.getString("update") ?: run { return true }
        val confFromPluginFolderUpdated = LocalDateTime.parse(confFromPluginFolderUpdatedStr) ?: run { return false }
        return confFromResourceUpdated.isAfter(confFromPluginFolderUpdated)
    }

    fun updateData(): Boolean {
        val willBeUpdated = willBeUpdated()
        if (willBeUpdated) {
            XCorePlugin.instance.saveResource("transferGuideData.yml", true)
            Bukkit.getLogger().info("[TransferGuide] XCore.jar内の路線データの方が新しいため、plugins/XCore内の路線データを上書きしました。")
        }
        return willBeUpdated
    }
}