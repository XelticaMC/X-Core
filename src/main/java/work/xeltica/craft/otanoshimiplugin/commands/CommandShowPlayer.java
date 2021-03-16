package work.xeltica.craft.otanoshimiplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import work.xeltica.craft.otanoshimiplugin.PlayerHideManager;

public class CommandShowPlayer extends CommandPlayerOnlyBase {
    public CommandShowPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        player.setMetadata("hidingPlayer", new FixedMetadataValue(plugin, false));
        PlayerHideManager.getInstance().updateAll(player);
        return true;
    }

    private Plugin plugin;
}
