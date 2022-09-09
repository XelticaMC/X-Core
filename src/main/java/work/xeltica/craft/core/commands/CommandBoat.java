package work.xeltica.craft.core.commands;

import java.util.List;

import com.destroystokyo.paper.block.TargetBlockInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.modules.hint.Hint;
import work.xeltica.craft.core.modules.hint.HintModule;
import work.xeltica.craft.core.stores.WorldStore;

/**
 * ボートを出現させるコマンド
 * @author Xeltica
 */
public class CommandBoat extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (!WorldStore.getInstance().canSummonVehicles(player.getWorld())) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。");
        }

        var spawnLoc = player.getLocation();

        final var lookBlock = player.getTargetBlock(5, TargetBlockInfo.FluidMode.ALWAYS);
        if (lookBlock != null && lookBlock.getType() != Material.AIR) {
            final var lookFace = player.getTargetBlockFace(5, TargetBlockInfo.FluidMode.ALWAYS);
            spawnLoc = lookBlock.getLocation();
            if (lookFace != null) {
                spawnLoc.add(lookFace.getDirection());
                spawnLoc.add(0.5, 0, 0.5);
            }
        }

        if (collidesAt(spawnLoc.clone())) {
            var flag = false;
            for (Vector offset: offsets) {
                if (!collidesAt(spawnLoc.clone().add(offset))) {
                    spawnLoc = spawnLoc.add(offset);
                    flag = true;
                    break;
                }
            }
            if (!flag) return Gui.getInstance().error(player, "狭すぎて置けない…");
        }

        final var boat = spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.BOAT, SpawnReason.CUSTOM);
        boat.setRotation(player.getLocation().getYaw(), boat.getLocation().getPitch());

        player.sendMessage("ボートを足元に召喚した。");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 2);

        HintModule.INSTANCE.achieve(player, Hint.BOAT);
        return true;
    }

    private boolean collidesAt(Location loc) {
        if (loc.getBlock().isSolid()) return true;
        for (Vector vec: collisionArea) {
            if (loc.clone().add(vec).getBlock().isSolid())
                return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        return COMPLETE_LIST_EMPTY;
    }

    private static final List<Vector> collisionArea = List.of(
            BlockFace.EAST.getDirection().multiply(0.7),
            BlockFace.WEST.getDirection().multiply(0.7),
            BlockFace.SOUTH.getDirection().multiply(0.7),
            BlockFace.NORTH.getDirection().multiply(0.7)
    );

    private static final List<Vector> offsets = List.of(
            BlockFace.NORTH.getDirection().multiply(0.35),
            BlockFace.NORTH_EAST.getDirection().multiply(0.35),
            BlockFace.EAST.getDirection().multiply(0.35),
            BlockFace.SOUTH_EAST.getDirection().multiply(0.35),
            BlockFace.SOUTH.getDirection().multiply(0.35),
            BlockFace.SOUTH_WEST.getDirection().multiply(0.35),
            BlockFace.WEST.getDirection().multiply(0.35),
            BlockFace.NORTH_WEST.getDirection().multiply(0.35)
    );
}
