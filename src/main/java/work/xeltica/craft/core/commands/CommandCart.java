package work.xeltica.craft.core.commands;

import java.util.List;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.WorldStore;

/**
 * トロッコを出現させるコマンド
 * @author Xeltica
 */
public class CommandCart extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (!WorldStore.getInstance().canSummonVehicles(player.getWorld())) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。");
        }

        final var lookBlock = player.getTargetBlock(5);

        if (lookBlock == null) return true;

        if (lookBlock.getType().data == Rail.class || lookBlock.getType().data == RedstoneRail.class) {
            final var spawnLoc = lookBlock.getLocation().add(0, 0.5,0);
            spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.MINECART, SpawnReason.CUSTOM);
            player.sendMessage("トロッコを召喚した。");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 2);

            HintStore.getInstance().achieve(player, Hint.MINECART);
        } else {
            return Gui.getInstance().error(player,"ここには召喚できない");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label,
                                      String[] args) {
        return COMPLETE_LIST_EMPTY;
    }
}
