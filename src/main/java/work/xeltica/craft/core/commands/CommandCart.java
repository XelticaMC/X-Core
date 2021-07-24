package work.xeltica.craft.core.commands;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.WorldStore;

public class CommandCart extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (!WorldStore.getInstance().canSummonVehicles(player.getWorld())) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。");
        }
        // if (!EbiPowerStore.getInstance().tryTake(player, 5)) {
        //     return Gui.getInstance().error(player, "§cEPが足りない…");
        // }

        var loc = player.getLocation();
        loc.getWorld().spawnEntity(loc, EntityType.MINECART, SpawnReason.CUSTOM);
        player.sendMessage("トロッコを足元に召喚した。");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 2);
        HintStore.getInstance().achieve(player, Hint.MINECART);
        return true;
    }
}
