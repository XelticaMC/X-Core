package work.xeltica.craft.core.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import work.xeltica.craft.core.stores.MobEPStore;

/**
 * ナイトメアワールドに関するハンドラーをまとめています。
 * @author Xeltica
 */
public class NightmareHandler implements Listener {
    public NightmareHandler() {
        superRareItems.add(Material.CREEPER_HEAD);
        superRareItems.add(Material.SKELETON_SKULL);
        superRareItems.add(Material.ZOMBIE_HEAD);
        superRareItems.add(Material.PLAYER_HEAD);
        superRareItems.add(Material.WITHER_SKELETON_SKULL);
        superRareItems.add(Material.DRAGON_HEAD);
        superRareItems.addAll(Tag.ITEMS_MUSIC_DISCS.getValues());
    }

    /**
     * ベッド爆弾を再現する
    */
    @EventHandler
    public void onPlayerUseBed(PlayerInteractEvent e) {
        final var block = e.getClickedBlock();
        if (block == null) return;
        final var loc = block.getLocation();
        if (!isNightmare(loc.getWorld())) return;

        if (Tag.BEDS.isTagged(block.getType())) {
            block.breakNaturally();
            e.setCancelled(true);
            loc.createExplosion(5, true);
        }
    }

    /**
     * レアアイテムの入手確率を下げる
     */
    @EventHandler
    public void onDropRareItems(EntityDeathEvent e) {
        if (!isNightmare(e.getEntity().getWorld())) return;
        if (random.nextDouble() > superRareItemsDropRatio) {
            final var drops = e.getDrops();
            drops.removeIf(st -> superRareItems.contains(st.getType()));
        }
    }

    /**
     * ナイトメアでの水没ダメージを抑制
     */
    @EventHandler
    public void onEntityTouchWater(EntityDamageEvent e) {
        if (!isNightmare(e.getEntity().getWorld())) return;
        if (e.getEntityType() == EntityType.PLAYER) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            e.setCancelled(true);
        }
    }

    /**
     * クリーパーが爆発ダメージで爆発するように
     */
    @EventHandler
    public void onCreeperPrim(EntityDamageEvent e) {
        if (!isNightmare(e.getEntity().getWorld())) return;
        if (e.getEntityType() != EntityType.CREEPER) return;
        final var creeper = (Creeper)e.getEntity();

        if (List.of(
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
        ).contains(e.getCause())) {
            creeper.ignite();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent e) {
        if (!isNightmare(e.getEntity().getWorld())) return;
        final var entity = e.getEntity();
        final var lastDamage = entity.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent lastEv) {
            final var damager = lastEv.getDamager();
            if (damager instanceof Player player) {
                MobEPStore.getInstance().playerKillEntity(player, entity, e);
            }
        }
    }

    private boolean isNightmare(World w) {
        // TODO ハードコーディングをやめる
        return w.getName().equals("nightmare2");
    }

    private final Random random = new Random();
    private final HashSet<Material> superRareItems = new HashSet<>();

    private final float superRareItemsDropRatio = 0.1f;
}
