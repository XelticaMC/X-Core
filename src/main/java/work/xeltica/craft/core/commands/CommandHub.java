package work.xeltica.craft.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.stores.HubStore;

import java.util.List;
/**
 * ロビーへ移動するコマンド
 * @author Xeltica
 */
public class CommandHub extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var store = HubStore.getInstance();

        // 現在いるワールドを踏まえてどのワールドに移動するかどうかを分岐する
        store.teleport(player, switch (player.getWorld().getName()) {
            default -> HubType.Main;
            case "hub" -> HubType.Classic;
            case "world" -> HubType.Classic;
            case "world_nether" -> HubType.Classic;
            case "world_the_end" -> HubType.Classic;
            case "nightmare" -> HubType.Classic;
            case "sandbox" -> HubType.Classic;
            case "wildarea" -> HubType.Classic;
        });

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label, String[] args) {
        return COMPLETE_LIST_EMPTY;
    }
}
