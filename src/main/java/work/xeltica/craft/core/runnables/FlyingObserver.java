package work.xeltica.craft.core.runnables;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 正規の手段で飛行していることを明確にするために、飛行状態のプレイヤーの足に
 * ジェットのようなパーティクルを出すためのバックグラウンドタスクです。
 * かなり動作が重たいために現在無効化しています。
 * TODO: 軽量化して再び有効化する
 * @author Xeltica
 */
public class FlyingObserver extends BukkitRunnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream().filter(p -> p.getAllowFlight() && p.getGameMode() != GameMode.SPECTATOR).forEach(p -> {
            final var loc = p.getLocation();
            final var world = p.getWorld();
            world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD, SpawnReason.CUSTOM, (e) -> {
                final var aec = (AreaEffectCloud)e;
                aec.setParticle(Particle.FLAME);
                aec.setRadius(0.5f);
                aec.setDuration(10);
            });
        });
    }
}
