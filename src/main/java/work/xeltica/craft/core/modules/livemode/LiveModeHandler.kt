package work.xeltica.craft.core.modules.livemode

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.modules.livemode.LiveModeModule.liveBarMap
import java.util.UUID

/**
 * ライブモードに関するイベントハンドラをまとめています。
 * @author kumitatepazuru
 */
class LiveModeHandler : Listener {
    /**
     * ライブモード中にプレイヤーが退出したときに一定時間後に自動的にライブモードを解除する関数
     * @param e イベント
     */
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        val uuid = p.uniqueId
        if (liveBarMap.containsKey(uuid)) {
            planToDeleteMap[uuid] = Bukkit.getScheduler().runTaskLater(instance, Runnable {
                LiveModeModule.setLiveMode(p, false)
                planToDeleteMap.remove(uuid)
            }, graceTimeTick)
        }
    }

    /**
     * 一旦退出した後一定期間以内に再ログインしたときに解除しないようにする関数
     * @param e イベント
     */
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val playerUUID = e.player.uniqueId
        planToDeleteMap[playerUUID]?.cancel()
    }

    private val planToDeleteMap: MutableMap<UUID, BukkitTask> = HashMap()
    private val graceTimeTick = 1200L
}