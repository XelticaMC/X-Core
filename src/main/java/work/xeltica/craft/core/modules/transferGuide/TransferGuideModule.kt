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

    /**
     * モジュール読み込み時に呼び出されるやつ
     */
    override fun onEnable() {
        updateData()
        verifyData()
    }

    /**
     * 路線データの整合性があるかないかを返します。
     * 整合性がある場合trueを返します。
     */
    private fun verifyData(): Boolean {
        val data = TransferGuideData()
        val logger = Bukkit.getLogger()
        var count = 0
        data.stations.forEach { station ->
            if (station.value.name == null) {
                logger.warning("[TransferGuideData(verifyData)] 駅の名前の欠如:stations.${station.key}.name")
                count++
            }
            if (station.value.yomi == null) {
                logger.warning("[TransferGuideData(verifyData)] 駅の読みの欠如:stations.${station.key}.yomi")
                count++
            }
            if (!data.availableWorlds.keys.contains(station.value.world)) {
                logger.warning("[TransferGuideData(verifyData)] 非対応のワールドに存在する駅:stations.${station.key}.world")
                count++
            }
            if (station.value.location.size != 2) {
                logger.warning("[TransferGuideData(verifyData)] 不正な座標データ:stations.${station.key}.location")
            }
            if (station.value.type == null) {
                logger.warning("[TransferGuideData(verifyData)] 駅の種類の欠如:stations.${station.key}.type")
                count++
            }
            station.value.paths.forEachIndexed { i, path ->
                if (path.to == null) {
                    logger.warning("[TransferGuideData(verifyData)] 行き先の存在しないpath:stations.${station.key}.paths[$i].to")
                    count++
                } else if (!data.stationExists(path.to)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${path.to}(stations.${station.key}.paths[$i].to)")
                    count++
                } else if (path.to == station.key) {
                    logger.warning("[TransferGuideData(verifyData)] 自駅への参照:stations.${station.key}.paths[$i].to")
                    count++
                }
                if (path.line == null) {
                    logger.warning("[TransferGuideData(verifyData)] 路線が指定されていないpath:stations.${station.key}.paths[$i].line")
                    count++
                } else if ((path.line != "walk" && path.line != "boat") && !data.lineExists(path.line)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない路線ID:${path.line}(stations.${station.key}.paths[$i].line)")
                    count++
                }
                if (path.direction == null) {
                    logger.warning("[TransferGuideData(verifyData)] 方向が指定されていないpath:stations.${station.key}.paths[$i].direction")
                    count++
                } else if (!data.directionExists(path.direction)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない方向ID:${path.direction}(stations.${station.key}.paths[$i].direction)")
                    count++
                }
                if (path.time == null) {
                    logger.warning("[TransferGuideData(verifyData)] 時間が指定されていないpath:stations.${station.key}.paths[$i].time")
                    count++
                } else if (path.time <= 0) {
                    logger.warning("[TransferGuideData(verifyData)] 無効な所要時間:${path.time}(stations.${station.key}.paths[$i].time)")
                    count++
                }
            }
        }
        val unlinkedStations = data.stations.toMutableMap()
        data.lines.forEach { line ->
            line.value.stations.forEachIndexed { i, station ->
                unlinkedStations.remove(station)
                if (!data.stationExists(station)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${station}(lines.${line.key}.stations[$i])")
                    count++
                }
            }
        }
        unlinkedStations.forEach { station ->
            if (station.value.paths.all { it.line != "walk" && it.line != "boat" }) {
                logger.warning("[TransferGuideData(verifyData)] どの路線にも属していない駅:stations.${station.key}")
                count++
            }
        }
        data.companies.forEach { company ->
            company.value.lines.forEachIndexed { i, line ->
                if (!data.lineExists(line)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない路線ID:${line}(companies.${company.key}.lines[$i])")
                    count++
                }
            }
        }
        data.municipalities.forEach { municipality ->
            municipality.value.stations.forEachIndexed { i, station ->
                if (!data.stationExists(station)) {
                    logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${station}(municipalities.${municipality.key}.stations[$i])")
                    count++
                }
            }
        }
        if (count != 0) {
            logger.warning("[TransferGuideData(verifyData)] ${count}個のデータ誤りが見つかりました。")
        } else {
            logger.info("[TransferGuideData(verifyData)] データに誤りは見つかりませんでした。")
        }
        return count == 0
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