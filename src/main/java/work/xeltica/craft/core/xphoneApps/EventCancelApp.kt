package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.models.PlayerRecord
import work.xeltica.craft.core.modules.NbsModule
import work.xeltica.craft.core.modules.PlayerStoreModule

/**
 * イベント用：タイムアタックを中止するアプリ
 */
class EventCancelApp : AppBase() {
    override fun getName(player: Player): String = "リタイアする"

    override fun getIcon(player: Player): Material = Material.BARRIER

    override fun onLaunch(player: Player) {
        val record = getRecord(player)

        record.delete(PlayerDataKey.PLAYING_COUNTER_ID)
        record.delete(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP)
        record[PlayerDataKey.PLAYED_COUNTER] = true

        player.sendMessage("カウントダウンをリタイアしました。")
        player.performCommand("respawn")
        NbsModule.stopRadio(player)
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event" && getRecord(player).getString(PlayerDataKey.PLAYING_COUNTER_ID) != null
    }

    private fun getRecord(player: Player): PlayerRecord {
        return PlayerStoreModule.open(player)
    }
}