package work.xeltica.craft.core.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.StructureType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.stores.ItemStore;

import java.util.List;

public class TicketWildareaBHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        final var store = ItemStore.getInstance();
        final var item = e.getItem();
        if (item == null) return;

        final var itemMeta = item.getItemMeta();
        if (itemMeta == null || itemMeta.displayName() == null) return;

        final var player = e.getPlayer();

        final var ticket = store.getItem(ItemStore.ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT);

        // 右クリック以外はガード
        if (!List.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.getAction())) return;

        if (!store.compareCustomItem(item, ticket)) {
            return;
        }

        e.setUseItemInHand(Event.Result.DENY);

        final var wildareab = Bukkit.getWorld("wildareab");
        if (wildareab == null) {
            Gui.getInstance().error(player, "テレポートに失敗しました。ワールドが生成されていません。");
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        player.sendMessage("旅行券を使用しました。現在手配中です。その場で少しお待ちください！");
        for (var pl : Bukkit.getOnlinePlayers()) {
            pl.sendMessage(String.format("§6%s§rさんがワイルドエリアB:海底神殿への旅に行きます！§b行ってらっしゃい！", player.getDisplayName()));
        }

        final var loc = wildareab.locateNearestStructure(wildareab.getSpawnLocation(), StructureType.OCEAN_MONUMENT, 200, true);
        if (loc == null) {
            Gui.getInstance().error(player, "みつかりませんでした。");
            return;
        }
        loc.setY(64);
        loc.getBlock().setType(Material.STONE);
        loc.setY(65);
        player.teleportAsync(loc);
    }
}
