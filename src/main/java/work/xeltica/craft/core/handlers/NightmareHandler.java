package work.xeltica.craft.core.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import work.xeltica.craft.core.gui.Gui;

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
        if (isNotNightmare(loc.getWorld())) return;

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
        if (isNotNightmare(e.getEntity().getWorld())) return;
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
        if (isNotNightmare(e.getEntity().getWorld())) return;
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
        if (isNotNightmare(e.getEntity().getWorld())) return;
        if (e.getEntityType() != EntityType.CREEPER) return;
        final var creeper = (Creeper)e.getEntity();

        if (List.of(
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
        ).contains(e.getCause())) {
            e.setCancelled(true);
            creeper.ignite();
        }
    }

    /**
     * テイム禁止
     */
    @EventHandler
    public void onBee(EntityTameEvent e) {
        if (isNotNightmare(e.getEntity().getWorld())) return;
        e.setCancelled(true);
    }

    /**
     * ブロックの設置を禁止
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isNotNightmare(e.getPlayer().getWorld())) return;
        e.setBuild(false);
    }

    /**
     * ブロックの破壊を禁止
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isNotNightmare(e.getPlayer().getWorld())) return;
        e.setCancelled(true);
    }

    private boolean isNotNightmare(World w) {
        // TODO ハードコーディングをやめる
        return !w.getName().equals("nightmare2");
    }

    private final Random random = new Random();
    private final HashSet<Material> superRareItems = new HashSet<>();

    private final float superRareItemsDropRatio = 0.1f;
}
