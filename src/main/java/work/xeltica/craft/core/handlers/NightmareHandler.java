package work.xeltica.craft.core.handlers;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    private boolean isNightmare(World w) {
        // TODO ハードコーディングをやめる
        return w.getName().equals("nightmare2");
    }

    private final Random random = new Random();
    private final HashSet<Material> superRareItems = new HashSet<>();

    private final float superRareItemsDropRatio = 0.1f;
}
