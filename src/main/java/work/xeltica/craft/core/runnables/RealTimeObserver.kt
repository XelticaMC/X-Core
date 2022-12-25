package work.xeltica.craft.core.runnables

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.events.RealTimeNewDayEvent
import java.time.LocalDateTime

/**
 * 現実時間を監視してイベントを発生させます。
 * 監視する時間には、サーバーOSのタイムゾーンを使用しています。
 * @author Xeltica
 */
class RealTimeObserver : BukkitRunnable() {
    override fun run() {
        val now = LocalDateTime.now()
        observeNewDay(now)
        previousDateTime = now
    }

    /**
     * 次の日になった瞬間を監視します。
     * X-Coreでは、日付変更を朝4時に行うものとします。
     * @param now 現在時刻
     */
    private fun observeNewDay(now: LocalDateTime) {
        if (now.hour == 4 && previousDateTime.hour != 4) {
            Bukkit.getPluginManager().callEvent(RealTimeNewDayEvent())
        }
    }

    private var previousDateTime: LocalDateTime = LocalDateTime.now()
}