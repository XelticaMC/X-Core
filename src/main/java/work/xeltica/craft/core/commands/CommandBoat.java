package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.HintStore;

public class CommandBoat extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var loc = player.getLocation();
        loc.getWorld().spawnEntity(loc, EntityType.BOAT, SpawnReason.CUSTOM);
        HintStore.getInstance().achieve(player, Hint.BOAT);
        return true;
    }
}
