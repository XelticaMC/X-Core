package work.xeltica.craft.core.modules.eventHalloween

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.player.PlayerBedEnterEvent
import work.xeltica.craft.core.modules.mobball.EntityMobBallHitEvent
import work.xeltica.craft.core.modules.eventHalloween.EventHalloweenModule.isEventMob

class EventHalloweenHandler : Listener {
    /**
     * モブがワールドに湧いた
     */
    @EventHandler
    fun onMobSpawnInWorld(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity !is Monster) return
        val world = entity.world
        // NOTE: プラグインで湧いたモブは除外（じゃないとreplaceMobメソッドが無限にこのイベントを呼ぶ）
        if (entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return
        // イベントワールドであり、メインワールドかつイベントモードの場合
        if (world.name == "event2" || (world.name == "main" && EventHalloweenModule.isEventMode)) {
            EventHalloweenModule.replaceMob(entity)
        }
    }

    /**
     * イベントモブがダメージを食らった
     */
    @EventHandler
    fun onEventMobDamaged(e: EntityDamageEvent) {
        // イベントモブでなければ対象外
        if (!e.entity.isEventMob()) return
        if (DAMAGE_CAUSE_LIST_PLAYER_KILLED.contains(e.cause) && e is EntityDamageByEntityEvent) {
            val damager = e.damager
            // プレイヤーが傷つけたか、プレイヤーが投じた発射物が傷つけた場合、即死
            if (damager is Player || (damager is Projectile && damager.shooter is Player)) {
                e.damage = 99999.0
            }
            return
        }
        // 奈落は普通に通す
        if (e.cause !== DamageCause.VOID) {
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
            val cause = e.entity.lastDamageCause?.cause
            if (cause != DamageCause.VOID) {
                Bukkit.getLogger()
                    .warning("イベントモブはプレイヤーキルか奈落によって死ぬべきだが、 ${cause ?: "(不明)"}を要因として死んだ。これは意図しない挙動なので、見つけ次第バグ報告をお願いします。")
            }
            return
        }
        EventHalloweenModule.replaceDrops(e.drops, killer)
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

    /**
     * イベント期間中は眠れないように
     */
    @EventHandler
    fun onPlayerBed(e: PlayerBedEnterEvent) {
        if (EventHalloweenModule.isEventMode && e.player.world.name == "main") {
            e.setUseBed(Event.Result.DENY)
            e.player.sendActionBar(Component.text("外はハロウィンのムードに包まれている。こんなテンションじゃ寝られない！"))
        }
    }

    private val DAMAGE_CAUSE_LIST_PLAYER_KILLED = listOf(
        DamageCause.ENTITY_ATTACK,
        DamageCause.ENTITY_SWEEP_ATTACK,
        DamageCause.SONIC_BOOM,
        DamageCause.PROJECTILE,
    )
}