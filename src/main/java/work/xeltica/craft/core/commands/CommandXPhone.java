package work.xeltica.craft.core.commands;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.stores.ItemStore;

/**
 * X Phoneを受け取るコマンド
 * @author Xeltica
 */
public class CommandXPhone extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var item = ItemStore.getInstance().getItem("xphone");
        if (item != null) {
            player.getInventory().addItem(item);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
            player.sendMessage(item.getItemMeta().displayName().append(Component.text("を付与しました")));
        }
        return true;
    }
}
