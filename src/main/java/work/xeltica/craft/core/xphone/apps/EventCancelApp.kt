package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.models.PlayerRecord
import work.xeltica.craft.core.stores.NbsStore
import work.xeltica.craft.core.stores.PlayerStore

/**
 * イベント用：タイムアタックを中止するアプリ
 */
class EventCancelApp : AppBase() {
    override fun getName(player: Player): String = "リタイアする"

    override fun getIcon(player: Player): Material = Material.BARRIER

    override fun onLaunch(player: Player) {
        val record = getRecord(player)
        val count = record.getInt(PlayerDataKey.PLAYED_COUNTER_COUNT)

        record.delete(PlayerDataKey.PLAYING_COUNTER_ID)
        record.delete(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP)
        record[PlayerDataKey.PLAYED_COUNTER_COUNT] = record.getInt(PlayerDataKey.PLAYED_COUNTER_COUNT, 0) + 1

        player.sendMessage("カウントダウンをリタイアしました。本日は${if (count == 0) "あと1回再チャレンジできます。" else "もう再チャレンジできません。"}")
        player.performCommand("respawn")
        NbsStore.getInstance().stopRadio(player)
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event" && getRecord(player).getString(PlayerDataKey.PLAYING_COUNTER_ID) != null
    }

    private fun getRecord(player: Player): PlayerRecord {
        return PlayerStore.getInstance().open(player)
    }
}