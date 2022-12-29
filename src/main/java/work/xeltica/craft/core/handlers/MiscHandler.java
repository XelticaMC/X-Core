package work.xeltica.craft.core.handlers;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * 軽微な機能に関するハンドラー。
 * 似たようなものが増えたら別ハンドラーにまとめる
 * @author Xeltica
 */
public class MiscHandler implements Listener {
    /**
     * 焼石製造機を丸石製造機にする
     * 採掘EPボーナスの自動化対策
     */
    @EventHandler
    public void onGuardCobbleStoneGenerator(BlockFormEvent e) {
        if (e.getNewState().getType() == Material.STONE) {
            e.getNewState().setType(Material.COBBLESTONE);
        }
    }

    /**
     * イベントマップでのダメージおよび死を防ぐ
     */
    @EventHandler
    public void onHurtInEventMap(EntityDamageEvent e) {
        // TODO: コンフィグで有効/無効を変更できるようにする
        // if (!(e.getEntity() instanceof final Player player)) return;
        // if ("event".equals(player.getWorld().getName())) {
        //     e.setCancelled(true);
        //     if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
        //         final var loc = player.getWorld().getSpawnLocation();
        //         player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        //     }
        // }
    }
}
