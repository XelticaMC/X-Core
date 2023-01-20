package work.xeltica.craft.core.modules.fly

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.scheduler.BukkitRunnable

/**
 * 正規の手段で飛行していることを明確にするために、飛行状態のプレイヤーの足に
 * ジェットのようなパーティクルを出すためのバックグラウンドタスクです。
 * @author Lutica
 */
class FlyingObserver : BukkitRunnable() {
    override fun run() {
        Bukkit.getOnlinePlayers().filter { it != null && it.allowFlight && it.gameMode == GameMode.SURVIVAL }
            .forEach {
                it.world.spawnParticle(Particle.FLAME, it.location, 16, 0.3, 0.0, 0.3, 0.1)
            }
    }
}