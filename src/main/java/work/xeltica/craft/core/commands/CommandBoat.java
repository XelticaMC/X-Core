package work.xeltica.craft.core.commands;

import java.util.List;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;
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

        final var loc = player.getLocation();
        loc.getWorld().spawnEntity(loc, EntityType.BOAT, SpawnReason.CUSTOM);

        player.sendMessage("ボートを足元に召喚した。");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 2);

        HintStore.getInstance().achieve(player, Hint.BOAT);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label,
            String[] args) {
        return COMPLETE_LIST_EMPTY;
    }
}
