package work.xeltica.craft.core.modules.transferPlayerData

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import java.util.UUID

object TransferPlayerDataModule : ModuleBase() {

    const val WARNING_MESSAGE: String =
        "§r引っ越しにより、次の情報が新しいプレイヤーに§l上書き§rされます。元のデータは削除されます。\n" +
                "> エビパワー\n" +
                "> ヒント解禁状況\n" +
                "> 各種設定項目"

    private val pendingMap = mutableMapOf<UUID, PendingData>()
    override fun onEnable() {

    }

    /**
     * [from] から [to] へのプレイヤーデータの引っ越しをリクエストします。
     */
    fun requestTransfer(from: Player, to: Player): PendingData {
        if (pendingMap.containsKey(from.uniqueId) || pendingMap.containsKey(to.uniqueId)) {
            throw Exception()
        }
        val pendingData = PendingData(from, to)
        pendingMap[from.uniqueId] = pendingData
        pendingMap[to.uniqueId] = pendingData
        from.sendMessage("引っ越しの申請をしました")
        to.sendMessage("引っ越しの受け取りができます")

        return pendingData
    }

    /**
     * このプレイヤーの引っ越し保留情報を取得します。
     */
    fun Player.getTransferPendingData(): PendingData? {
        return pendingMap[uniqueId]
    }

    /**
     * このプレイヤーの引っ越し状況を取得します。
     */
    fun Player.getTransferState(): TransferState {
        val data = pendingMap[uniqueId] ?: return TransferState.NONE
        return if (data.from.uniqueId == uniqueId) TransferState.FROM else TransferState.TO
    }

    /**
     * 引っ越しを開始します。
     */
    fun PendingData.process() {
        from.sendMessage("引っ越しを開始します")
        to.sendMessage("引っ越しを開始します")

        transferPlayerStoreData(from, to)
        // Bukkit イベントを呼び出して各モジュールでデータ移行をやっていく
        val e = TransferPlayerDataEvent(from, to)
        Bukkit.getPluginManager().callEvent(e)

        from.kick(Component.text("お使いのアカウントは、${to.name}への引っ越しが完了しました"))
        pendingMap.remove(from.uniqueId)
        to.sendMessage("引っ越しが完了しました")
        pendingMap.remove(to.uniqueId)
    }

    /**
     * 引っ越しをキャンセルします。
     */
    fun PendingData.cancel() {
        from.sendMessage("引っ越しがキャンセルされました")
        pendingMap.remove(from.uniqueId)
        to.sendMessage("引っ越しがキャンセルされました")
        pendingMap.remove(to.uniqueId)
    }

    private fun transferPlayerStoreData(from: Player, to: Player) {
        val fromPlayerRecord = PlayerStore.open(from.uniqueId)
        val toPlayerRecord = PlayerStore.open(to.uniqueId)
        for ((key, value) in fromPlayerRecord.getAll()) {
            toPlayerRecord[key] = value
            fromPlayerRecord.delete(key)
        }
    }
}
