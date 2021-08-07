package work.xeltica.craft.core.runnables;

import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.events.RealTimeNewDayEvent;

/**
 * 現実時間を監視してイベントを発生させます。
 * 監視する時間には、サーバーOSのタイムゾーンを使用しています。
 * @author Xeltica
 */
public class RealTimeObserver extends BukkitRunnable {
    @Override
    public void run() {
        var now = LocalDateTime.now();
        observeNewDay(now);
        previousDateTime = now;
    }

    /**
     * 次の日になった瞬間を監視します。
     * X-Coreでは、日付変更を朝4時に行うものとします。
     * @param now 現在時刻
     */
    private void observeNewDay(LocalDateTime now) {
        if (now.getHour() == 4 && previousDateTime.getHour() != 4) {
            Bukkit.getPluginManager().callEvent(new RealTimeNewDayEvent());
        }
    }

    private LocalDateTime previousDateTime;
}
