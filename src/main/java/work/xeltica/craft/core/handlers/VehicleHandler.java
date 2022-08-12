package work.xeltica.craft.core.handlers;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.modules.HintModule;
import work.xeltica.craft.core.stores.VehicleStore;
import work.xeltica.craft.core.stores.WorldStore;

import java.util.List;

/**
 * 乗り物に関するハンドラーをまとめています。
 * @author Xeltica
 */
public class VehicleHandler implements Listener {
    @EventHandler
    public void onEnter(VehicleEnterEvent e) {
        VehicleStore.getInstance().unregisterVehicle(e.getVehicle());
    }

    @EventHandler
    public void onExit(VehicleExitEvent e) {
        VehicleStore.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onCreated(VehicleCreateEvent e) {
        VehicleStore.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onDestroyed(VehicleDestroyEvent e) {
        final var v = e.getVehicle();
        if (VehicleStore.getInstance().isValidVehicle(v)) {
            e.setCancelled(true);
            e.getVehicle().remove();
        }
        VehicleStore.getInstance().unregisterVehicle(v);
    }

    @EventHandler
    public void onVehicleDestroyed(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.MINECART || e.getEntityType() == EntityType.BOAT) {
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onPlayerSpawnVehicle(PlayerInteractEvent e) {
        // クリエイティブワールドは対象外とする
        if (WorldStore.getInstance().isCreativeWorld(e.getPlayer().getWorld())) return;

        final var block = e.getClickedBlock();
        if (block == null) return;
        if (vehicleItems.contains(e.getMaterial()) && e.getBlockFace() == BlockFace.UP) {
            final var loc = block.getLocation().add(e.getBlockFace().getDirection());
            loc.getWorld().spawnParticle(Particle.ASH, loc, 8, 1, 1, 1);
            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
            final var isCart = e.getMaterial() == Material.MINECART;
            if (!HintModule.hasAchieved(e.getPlayer(), isCart ? Hint.MINECART : Hint.BOAT)) {
                e.getPlayer().sendMessage("§a" + (isCart ? "トロッコ" : "ボート") + "§rは§bX Phone§rを用いてどこでも召喚できます。X Phoneをお持ちでなければ§a/phone§rコマンドで入手できます。");
            }
            e.setCancelled(true);
        }
    }

    private final List<Material> vehicleItems = List.of(
            Material.ACACIA_BOAT,
            Material.BIRCH_BOAT,
            Material.DARK_OAK_BOAT,
            Material.OAK_BOAT,
            Material.JUNGLE_BOAT,
            Material.SPRUCE_BOAT,
            Material.MINECART
    );
}
