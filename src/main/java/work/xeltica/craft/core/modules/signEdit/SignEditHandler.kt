package work.xeltica.craft.core.modules.signEdit

import org.bukkit.block.Sign
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class SignEditHandler : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSignClick(e: PlayerInteractEvent) {
        // 右クリックでない or スニークしていない なら中止
        if (e.action !== Action.RIGHT_CLICK_BLOCK || !e.player.isSneaking) return
        // 既にキャンセルされているなら中止
        if (e.useInteractedBlock() == Event.Result.DENY) return
        // クリックしたブロックが看板でなければ中止
        val sign = e.clickedBlock?.state as? Sign ?: return

        e.player.openSign(sign)
    }
}