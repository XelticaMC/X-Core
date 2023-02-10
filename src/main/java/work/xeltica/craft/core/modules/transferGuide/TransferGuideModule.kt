package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData
import java.time.LocalDateTime

/**
 * 乗換案内アプリ用モジュール
 * @author Knit prg.
 */
object TransferGuideModule : ModuleBase() {

    lateinit var data: TransferGuideData

    /**
     * モジュール読み込み時に呼び出されるやつ
     */
    override fun onEnable() {
        updateData()
        data = TransferGuideData()
    }

    /**
     * 路線データの更新が必要かどうかを確認します。
     */
    private fun willBeUpdated(): Boolean {
        val instanceResource = XCorePlugin.instance.getResource("transferGuideData.yml") ?: return false
        val instanceResourceUpdatedStr =
            YamlConfiguration.loadConfiguration(instanceResource.reader()).getString("update") ?: return false
        val instanceResourceUpdated = LocalDateTime.parse(instanceResourceUpdatedStr) ?: return false
        val pluginFolderUpdatedStr = Config("transferGuideData").conf.getString("update") ?: return true
        val pluginFolderUpdated = LocalDateTime.parse(pluginFolderUpdatedStr) ?: return false
        return instanceResourceUpdated.isAfter(pluginFolderUpdated)
    }

    /**
     * 路線データの更新が必要な場合に更新を行います。
     */
    private fun updateData(): Boolean {
        val willBeUpdated = willBeUpdated()
        if (willBeUpdated) {
            XCorePlugin.instance.saveResource("transferGuideData.yml", true)
            Bukkit.getLogger().info("[TransferGuide] XCore.jar内の路線データの方が新しいため、plugins/XCore内の路線データを上書きしました。")
        }
        return willBeUpdated
    }
}