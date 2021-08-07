package work.xeltica.craft.core.handlers;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import work.xeltica.craft.core.events.PlayerCounterFinish;
import work.xeltica.craft.core.events.PlayerCounterStart;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.CounterData;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.CounterStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.utils.Time;

/**
 * Counter API処理用ハンドラー
 */
public class CounterHandler implements Listener {

    /**
     * カウンター登録モードのときに感圧板を右クリックした
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClickPlate(PlayerInteractEvent e) {
        final var isBlockClick = List.of(
            Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_BLOCK
        ).contains(e.getAction());

        final var logger = Bukkit.getLogger();

        logger.info("isBlockClick: " + isBlockClick);

        // ブロッククリックでなければ無視
        if (!isBlockClick) return;

        final var pstore = PlayerStore.getInstance();
        final var store = CounterStore.getInstance();
        final var ui = Gui.getInstance();

        final var player = e.getPlayer();
        final var block = e.getClickedBlock();

        final var record = pstore.open(player);

        final var isCounterRegisterMode = record.getBoolean(PlayerDataKey.COUNTER_REGISTER_MODE);
        final var isPlate = Tag.PRESSURE_PLATES.isTagged(block.getType());
        logger.info("isCounterRegisterMode: " + isCounterRegisterMode);
        logger.info("isPlate: " + isPlate);

        // カウンター登録モードでなければ無視
        if (!isCounterRegisterMode) return;

        // 感圧板でなければ無視
        if (!isPlate) return;

        final var name = record.getString(PlayerDataKey.COUNTER_REGISTER_NAME);
        final var loc = record.getLocation(PlayerDataKey.COUNTER_REGISTER_LOCATION);
        final var daily = record.getBoolean(PlayerDataKey.COUNTER_REGISTER_IS_DAILY);

        e.setCancelled(true);

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

        // 感圧板でなければ無視
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) return;

        var first = store.getByLocation1(block.getLocation());
        var last = store.getByLocation2(block.getLocation());

        final var record = pstore.open(player);
        final var counterId = record.getString(PlayerDataKey.PLAYING_COUNTER_ID);
        final var startedAt = Long.parseLong(record.getString(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP, "0"));
        final var counter = counterId == null ? null : store.get(counterId);

        final var isUsingCounter = counter != null;

        try {
            // カウンター開始する
            if (first != null) {
                if (isUsingCounter) {
                    ui.error(player, "既にカウントが始まっています！");
                    return;
                }

                if (record.getBoolean(PlayerDataKey.PLAYED_COUNTER)) {
                    ui.error(player, "本日は既に挑戦済みです！また明日遊んでね。");
                    return;
                }

                final var ts = Long.toString(System.currentTimeMillis());
                record.set(PlayerDataKey.PLAYING_COUNTER_ID, store.getIdOf(first), false);
                record.set(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP, ts, false);
                pstore.save();
                
                player.showTitle(Title.title(Component.text("§6スタート！"), Component.empty()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 2);

                Bukkit.getPluginManager().callEvent(new PlayerCounterStart(player, counter));
            }
            
            // カウンター終了する
            if (last != null) {
                if (!isUsingCounter) {
                    ui.error(player, "こちらはゴールです。スタートから開始してください。");
                    return;
                }
                if (!store.getIdOf(last).equals(counterId)) {
                    ui.error(player, "ゴールが異なります。");
                    return;
                }
                record.delete(PlayerDataKey.PLAYING_COUNTER_ID, false);
                record.delete(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP, false);
                record.set(PlayerDataKey.PLAYED_COUNTER, true, false);
                pstore.save();

                final var endAt = System.currentTimeMillis();
                final var diff = endAt - startedAt;
                final var timeString = Time.msToString(diff);

                player.sendMessage("ゴール！タイムは" + timeString + "でした。");

                player.showTitle(Title.title(
                    Component.text("§6ゴール！"),
                    Component.text("タイム " + timeString)
                ));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 2);

                Bukkit.getPluginManager().callEvent(new PlayerCounterFinish(player, counter, diff));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            ui.error(player, "§cIO エラーが発生したために処理を続行できませんでした。");
        }
    }
}
