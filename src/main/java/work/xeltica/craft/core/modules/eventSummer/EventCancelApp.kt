package work.xeltica.craft.core.modules.eventSummer

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.playerStore.PlayerRecord
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * イベント用：タイムアタックを中止するアプリ
 */
class EventCancelApp : AppBase() {
    override fun getName(player: Player): String = "リタイアする"

    override fun getIcon(player: Player): Material = Material.BARRIER

    override fun onLaunch(player: Player) {
        val record = getRecord(player)
        val count = record.getInt(CounterModule.PS_KEY_COUNT)

        record.delete(CounterModule.PS_KEY_ID)
        record.delete(CounterModule.PS_KEY_TIME)
        record[CounterModule.PS_KEY_COUNT] = record.getInt(CounterModule.PS_KEY_COUNT, 0) + 1

        player.sendMessage("カウントダウンをリタイアしました。本日は${if (count == 0) "あと1回再チャレンジできます。" else "もう再チャレンジできません。"}")
        player.performCommand("respawn")
        NbsModule.stopRadio(player)
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name == "event" && getRecord(player).getString(CounterModule.PS_KEY_ID) != null
    }

    private fun getRecord(player: Player): PlayerRecord {
        return PlayerStore.open(player)
    }
}