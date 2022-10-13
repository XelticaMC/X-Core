package work.xeltica.craft.core.modules.halloween

import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import work.xeltica.craft.core.events.EntityMobBallHitEvent
import work.xeltica.craft.core.modules.halloween.HalloweenModule.isEventMob

class HalloweenHandler : Listener {
    /**
     * モブがワールドに湧いた
     */
    @EventHandler
    fun onMobSpawnInWorld(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity !is Monster) return
        val world = entity.world
        // イベントワールドでない、あるいはメインワールドだがイベントモードでないなら除外
        if (world.name != "event2" || (world.name == "main" && !HalloweenModule.isEventMode)) return
        // NOTE: プラグインで湧いたモブは除外（じゃないとreplaceMobメソッドが無限にこのイベントを呼ぶ）
        if (entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return
        HalloweenModule.replaceMob(entity)
    }

    /**
     * イベントモブがダメージを食らった
     */
    @EventHandler
    fun onEventMobDamaged(e: EntityDamageEvent) {
        // イベントモブでなければ対象外
        if (!e.entity.isEventMob()) return
        // プレイヤーに傷つけられた場合は即死
        if (e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e is EntityDamageByEntityEvent && e.damager.type == EntityType.PLAYER) {
            e.damage = 99999.0
            return
        }
        // 奈落は普通に通す
        if (e.cause !== EntityDamageEvent.DamageCause.VOID) {
            e.isCancelled = true
        }
    }

    /**
     * イベントモブがくたばった
     */
    @EventHandler
    fun onEventMobDeath(e: EntityDeathEvent) {
        // イベントモブでなければ対象外
        if (!e.entity.isEventMob()) return
        val killer = e.entity.killer
        if (killer == null) {
            if (e.entity.lastDamageCause?.cause != EntityDamageEvent.DamageCause.VOID) {
                Bukkit.getLogger()
                    .warning("イベントモブはプレイヤーキルか奈落によって死ぬべきだが、 ${e.entity.lastDamageCause?.cause ?: "(unknown)"}を要因として死んだ。これは意図しない挙動なので、見つけ次第バグ報告をお願いします。")
            }
            return
        }
        HalloweenModule.replaceDrops(e.drops, killer)
    }

    /**
     * イベントモブにモブボールを当てようとした
     * （モブボールは動作をおかしくしないために当てれないようにしてる）
     */
    @EventHandler
    fun onEventMobHitMobBall(e: EntityMobBallHitEvent) {
        if (e.target.isEventMob()) {
            e.isCancelled = true
        }
    }
}