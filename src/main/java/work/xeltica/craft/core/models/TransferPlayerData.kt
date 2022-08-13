package work.xeltica.craft.core.models

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import work.xeltica.craft.core.modules.EbipowerModule
import work.xeltica.craft.core.modules.HintModule
import work.xeltica.craft.core.modules.PlayerStoreModule
import java.io.IOException
import work.xeltica.craft.core.modules.CloverModule
import java.util.HashMap
import java.util.UUID

class TransferPlayerData(val from: Player, val to: Player) {
    enum class TransferPlayerType {
        FROM_PLAYER, TO_PLAYER
    }

    fun getType(player: Player): TransferPlayerType? {
        return if (player.uniqueId === from.uniqueId) {
            TransferPlayerType.FROM_PLAYER
        } else if (player.uniqueId === to.uniqueId) {
            TransferPlayerType.TO_PLAYER
        } else {
            null
        }
    }

    fun process() {
        from.sendMessage("引っ越しを開始します")
        to.sendMessage("引っ越しを開始します")
        transferEbiPower()
        transferHint()
        transferPlayerStoreData()
        transferClover()
        to.sendMessage("引っ越しが完了しました")
        from.kick(Component.text("引っ越しが完了しました"))
        close()
    }

    fun cancel() {
        from.sendMessage("引っ越しがキャンセルされました")
        to.sendMessage("引っ越しがキャンセルされました")
        close()
    }

    fun close() {
        standby.remove(from.uniqueId)
        standby.remove(to.uniqueId)
    }

    private fun transferEbiPower() {
        val hasMoney = EbipowerModule[from]
        EbipowerModule.tryTake(from, hasMoney)
        EbipowerModule.tryGive(to, hasMoney)
    }

    private fun transferHint() {
        for (hintName in HintModule.getArchived(from)) {
            for (hint in Hint.values()) {
                if (hint.hintName == hintName) {
                    HintModule.achieve(to, hint, false)
                    break
                }
            }
        }
        HintModule.deleteArchiveData(from)
    }

    private fun transferPlayerStoreData() {
        val fromPlayerRecord = PlayerStoreModule.open(from.uniqueId)
        val toPlayerRecord = PlayerStoreModule.open(to.uniqueId)
        for (key in PlayerDataKey.values()) {
            toPlayerRecord[key] = fromPlayerRecord[key]
            fromPlayerRecord.delete(key)
        }
        try {
            PlayerStoreModule.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun transferClover() {
        val hasClover = CloverModule.getCloverOf(from)
        CloverModule.set(to, hasClover)
        CloverModule.delete(from)
    }

    init {
        standby[from.uniqueId] = this
        standby[to.uniqueId] = this
        from.sendMessage("引っ越しの申請をしました")
        to.sendMessage("引っ越しの受け取りができます")
    }

    companion object {
        fun getInstance(player: Player): TransferPlayerData? {
            return standby[player.uniqueId]
        }

        private val standby: MutableMap<UUID, TransferPlayerData> = HashMap()
    }
}