package work.xeltica.craft.core.runnables

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.player.PlayerModule
import work.xeltica.craft.core.utils.Time

/**
 * タイムアタック走者に時間を出すプログラム
 */
class TimeAttackObserver : BukkitRunnable() {
    override fun run() {
        // TODO パフォーマンス改善のため、走者をイベントハンドラーでリスト化し、キャッシュするようにする
        Bukkit.getOnlinePlayers().forEach {
            val record = PlayerModule.open(it)
            if (!record.has(PlayerDataKey.PLAYING_COUNTER_ID)) return@forEach

            val ts = record.getString(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP, "0")?.toLong()!!
            val now = System.currentTimeMillis()
            val timeString = Time.msToString(now - ts)
            it.sendActionBar(Component.text(timeString))
        }
    }
}