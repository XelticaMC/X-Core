package work.xeltica.craft.core.handlers;

import io.papermc.paper.event.block.TargetHitEvent;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
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
        if (x == -35 && y == 74 && z == -369 && "main".equals(loc.getWorld().getName())) {
            if (e.getEntity().getShooter() instanceof Player p) {
                HintStore.getInstance().achieve(p, Hint.TAIKO);
            }
        }
    }

    /**
     * お祭り期間中寝かせない
     */
    @EventHandler
    public void onSleep(PlayerBedEnterEvent e) {
        if (e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        if (!"main".equals(e.getPlayer().getWorld().getName())) return;

        e.setUseBed(Event.Result.DENY);
        final var player = e.getPlayer();
        player.sendMessage("あたりは夏祭りムード。こんなときに眠っちゃいられない！");
        player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_HURT_LAND, SoundCategory.PLAYERS, 1, 1);
    }
}
