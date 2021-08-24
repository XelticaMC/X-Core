package work.xeltica.craft.core.handlers;

import io.papermc.paper.event.block.TargetHitEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;

/**
 * 軽微な機能に関するハンドラー。
 * @author Xeltica
 */
public class MiscHandler implements Listener {
    /**
     * Hint.TAIKO を解除するためのハンドラー
     */
    @EventHandler
    public void onTargetHit(TargetHitEvent e) {
        final var block = e.getHitBlock();
        if (block == null) return;
        final var loc = block.getLocation();
        final var x = loc.getBlockX();
        final var y = loc.getBlockY();
        final var z = loc.getBlockZ();
        // NOTE: イベント終わったら破棄するコードなので、的の位置をハードコーディングしています
        if (x == -35 && y == 74 && z == -369) {
            if (e.getEntity().getShooter() instanceof Player p) {
                HintStore.getInstance().achieve(p, Hint.TAIKO);
            }
        }
    }
}
