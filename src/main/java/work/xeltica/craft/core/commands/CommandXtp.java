package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.commands.CommandBase;
import work.xeltica.craft.core.modules.world.WorldModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 指定したワールドの最後にいた場所に転送するコマンド
 * @author Xeltica
 */
public class CommandXtp extends CommandBase {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
        if (args.length == 2 && !sender.hasPermission("otanoshimi.command.xtp.other")) {
            sender.sendMessage("§c権限がありません。");
            return true;
        }
        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage("プレイヤーが実行してください。");
            return true;
        }
        final var worldName = args[0];
        final var p = args.length == 2 ? Bukkit.getPlayer(args[1]) : (Player)sender;
        if (p == null) {
            sender.sendMessage("§cプレイヤーが存在しません");
            return true;
        }
        WorldModule.INSTANCE.teleportToSavedLocation(p, worldName);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            final var worlds = XCorePlugin.getInstance().getServer().getWorlds().stream().map(World::getName).toList();
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[0], worlds, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            return null;
        }
        return COMPLETE_LIST_EMPTY;
    }
}
