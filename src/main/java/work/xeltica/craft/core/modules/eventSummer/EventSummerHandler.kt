package work.xeltica.craft.core.modules.eventSummer

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.counter.PlayerCounterFinish
import work.xeltica.craft.core.modules.counter.PlayerCounterStart
import work.xeltica.craft.core.modules.nbs.NbsModel
import work.xeltica.craft.core.modules.nbs.NbsModule.playRadio
import work.xeltica.craft.core.modules.nbs.NbsModule.stopRadio
import work.xeltica.craft.core.utils.Ticks

class EventSummerHandler : Listener {
    @EventHandler
    fun onNewDay(e: RealTimeNewDayEvent) {
        // ログボ記録を削除
        PlayerStore.openAll().forEach {
            it.delete(EventSummerModule.PS_KEY_LOGIN_BONUS_SUMMER)
        }
    }

    /**
     * タイムアタック開始時に音を鳴らす機能
     */
    @EventHandler
    fun onCounterStart(e: PlayerCounterStart) {
        val player = e.player
        if (player.world.name != "event") return
        playRadio(player, "submerged3", NbsModel.PlaybackMode.LOOP)
    }

    /**
     * イベントマップ：TA終了イベント
     */
    @EventHandler
    fun onCounterFinish(e: PlayerCounterFinish) {
        val player = e.player
        if (player.world.name != "event") return
        stopRadio(player)
        Bukkit.getScheduler().runTaskLater(instance, Runnable {
            player.sendMessage("${ChatColor.AQUA}メインワールドに戻る場合は、X Phoneをお使いください。")
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1.2f)
        }, Ticks.from(3.0).toLong())
    }

    /**
     * イベント期間中は眠れないように
     */
    @EventHandler
    fun onPlayerBed(e: PlayerBedEnterEvent) {
        if (EventSummerModule.isEventNow() && e.player.world.name == "main") {
            e.setUseBed(Event.Result.DENY)
            e.player.sendMessage(ChatColor.RED.toString() + "お祭りムードに溢れている。こんなテンションじゃ寝られない！")
        }
    }
}