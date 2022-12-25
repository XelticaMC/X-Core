package work.xeltica.craft.core.runnables

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.events.NewMorningEvent

/**
 * 朝になったタイミングで NewMorningEvent イベントを発行するための
 * バックグラウンドタスクです。
 * @author Xeltica
 */
class DaylightObserver : BukkitRunnable() {
    override fun run() {
        val time = Bukkit.getWorld("main")?.time ?: return
        if (time < prevTime) {
            // 時間が前よりも小さくなったのであれば、おそらく日をまたいだことになる
            val event = NewMorningEvent(time)
            Bukkit.getPluginManager().callEvent(event)
        }
        prevTime = time
    }

    private var prevTime: Long = 0
}