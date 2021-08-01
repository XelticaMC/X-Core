package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.stores.ItemStore;

/**
 * カスタムアイテムをgiveするコマンド
 * @author Xeltica
 */
public class CommandGiveCustomItem extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        var name = args.length >= 1 ? args[0] : null;
        var p = name == null ? player : Bukkit.getPlayer(name);
        var store = ItemStore.getInstance();
        if (p == null) {
            player.sendMessage(ChatColor.RED + "そのようなプレイヤーはいません");
            return true;
        }
        try {
            var typeString = args.length >= 2 ? args[1] : "";
            var item = store.getItem(typeString.toLowerCase());
            if (item != null) {
                p.getInventory().addItem(item);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
                p.sendMessage(item.getItemMeta().displayName().append(Component.text("を付与しました")));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("引数がおかしい");
            return true;
        }
        return true;
    }
    
}
