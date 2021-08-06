package work.xeltica.craft.core.handlers;

import java.io.IOException;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.CounterData;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.CounterStore;
import work.xeltica.craft.core.stores.PlayerStore;

/**
 * Counter API処理用ハンドラー
 */
public class CounterHandler implements Listener {

    /**
     * カウンター登録モードのときに感圧板を右クリックした
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClickPlate(PlayerInteractEvent e) {
        // 右クリックでなければ無視
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final var pstore = PlayerStore.getInstance();
        final var store = CounterStore.getInstance();
        final var ui = Gui.getInstance();

        final var player = e.getPlayer();
        final var block = e.getClickedBlock();

        final var record = pstore.open(player);

        // カウンター登録モードでなければ無視
        if (!record.getBoolean(PlayerDataKey.COUNTER_REGISTER_MODE)) return;

        // 感圧板でなければ無視
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) return;

        final var name = record.getString(PlayerDataKey.COUNTER_REGISTER_NAME);
        final var loc = record.getLocation(PlayerDataKey.COUNTER_REGISTER_LOCATION);
        final var daily = record.getBoolean(PlayerDataKey.COUNTER_REGISTER_IS_DAILY);

        e.setCancelled(false);

        try {
            // 始点登録
            if (loc == null) {
                record.set(PlayerDataKey.COUNTER_REGISTER_LOCATION, block.getLocation());
                player.sendMessage("始点を登録しました。続いて終点を登録します。");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 2);
            } else {
                store.add(new CounterData(name, loc, block.getLocation(), daily, null));
                player.sendMessage("カウンター " + name + "を登録しました。");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 1);
                record.delete(PlayerDataKey.COUNTER_REGISTER_MODE, false);
                record.delete(PlayerDataKey.COUNTER_REGISTER_NAME, false);
                record.delete(PlayerDataKey.COUNTER_REGISTER_LOCATION, false);
                record.delete(PlayerDataKey.COUNTER_REGISTER_IS_DAILY, false);
                pstore.save();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            ui.error(player, "§cIO エラーが発生したために処理を続行できませんでした。");
        }
    }
    /**
     * カウンター感圧板を踏んだ
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onUsePlate(PlayerInteractEvent e) {
        // 踏んだわけじゃないのなら無視
        if (e.getAction() != Action.PHYSICAL) return;

        final var pstore = PlayerStore.getInstance();
        final var store = CounterStore.getInstance();
        final var ui = Gui.getInstance();

        final var player = e.getPlayer();
        final var block = e.getClickedBlock();

        // ブロックがnullなら無視
        if (block == null) return;

        final var record = pstore.open(player);

        // 感圧板でなければ無視
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) return;

        var first = store.getByLocation1(block.getLocation());
        var last = store.getByLocation2(block.getLocation());
        
        if (first != null) {
            player.sendMessage("カウンター " + first.getName() + " の始点です");
        }
        
        if (last != null) {
            player.sendMessage("カウンター " + last.getName() + " の終点です");
        }
    }
}
