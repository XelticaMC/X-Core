package work.xeltica.craft.core.models

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.clover.CloverModule
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
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
        val ebiPowerModule = EbiPowerModule
        val hasMoney = ebiPowerModule.get(from)
        ebiPowerModule.tryTake(from, hasMoney)
        ebiPowerModule.tryGive(to, hasMoney)
    }

    private fun transferHint() {
        val hintModule = HintModule
        for (hintName in hintModule.getArchived(from)) {
            for (hint in Hint.values()) {
                if (hint.hintName == hintName) {
                    hintModule.achieve(to, hint, false)
                    break
                }
            }
        }
        hintModule.deleteArchiveData(from)
    }

    private fun transferPlayerStoreData() {
        val fromPlayerRecord = PlayerStore.open(from.uniqueId)
        val toPlayerRecord = PlayerStore.open(to.uniqueId)
        for ((key, value) in fromPlayerRecord.getAll()) {
            toPlayerRecord[key] = value
            fromPlayerRecord.delete(key)
        }
    }

    private fun transferClover() {
        val cloverStore = CloverModule
        val hasClover = cloverStore.getCloverOf(from)
        cloverStore[to] = hasClover
        cloverStore.delete(from)
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