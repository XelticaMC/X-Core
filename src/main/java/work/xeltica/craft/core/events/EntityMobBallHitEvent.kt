package work.xeltica.craft.core.events

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class EntityMobBallHitEvent(
    val target: LivingEntity,
    val thrownBall: Projectile
): Event(), Cancellable {

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }

    override fun isCancelled(): Boolean = _isCancelled

    override fun setCancelled(cancel: Boolean) {
        _isCancelled = cancel
    }

    companion object {
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }

        @JvmStatic
        private val HANDLERS_LIST = HandlerList()
    }

    private var _isCancelled = false
}