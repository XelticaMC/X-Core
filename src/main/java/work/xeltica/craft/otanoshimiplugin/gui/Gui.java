package work.xeltica.craft.otanoshimiplugin.gui;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Gui implements Listener {
    public static Gui getInstance() {
        return instance == null ? (instance = new Gui()) : instance;
    }

    public void openMenu(Player player, String title, MenuItem... items) {
        // TODO 統合版を相手取る場合Form APIが使えるようになったら良い感じにやる
        openMenuJavaImpl(player, title, items);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        var inv = e.getInventory();
        var p = e.getWhoClicked();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;
        e.setCancelled(true);

        var menuItems = invMap.get(inv);
        var id = e.getSlot();
    
        if (menuItems.length <= id) return;
        p.closeInventory();
        var handler = menuItems[id].getOnClick();
        if (handler != null) handler.accept(menuItems[id]);
    }

    public void onInventoryClose(InventoryCloseEvent e) {
        var inv = e.getInventory();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv))
            return;
        
        // GC
        invMap.remove(inv);
    }

    private void openMenuJavaImpl(Player player, String title, MenuItem[] items) {
        var inv = Bukkit.createInventory(null, (1 + items.length / 9) * 9, title);

        Arrays.stream(items).map(i -> {
            var item = new ItemStack(i.getIcon(), i.getCount());
            var meta = item.getItemMeta();
            meta.setDisplayName(i.getName());
            item.setItemMeta(meta);

            return item;
        }).forEach(i -> inv.addItem(i));

        invMap.put(inv, items);
        player.openInventory(inv);        
    }

    private static final HashMap<Inventory, MenuItem[]> invMap = new HashMap<>();
    private static Gui instance;
}
