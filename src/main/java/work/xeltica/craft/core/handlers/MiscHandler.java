package work.xeltica.craft.core.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.Ticks;
import work.xeltica.craft.core.api.events.PlayerCounterFinish;
import work.xeltica.craft.core.api.events.PlayerCounterStart;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.NbsModel;
import work.xeltica.craft.core.modules.CustomItemModule;
import work.xeltica.craft.core.modules.NbsModule;
import work.xeltica.craft.core.utils.EventUtility;

import java.util.Arrays;
import java.util.Objects;

/**
 * 軽微な機能に関するハンドラー。
 * 似たようなものが増えたら別ハンドラーにまとめる
 * @author Xeltica
 */
public class MiscHandler implements Listener {
    /**
     * カスタムアイテムのクラフト材料としての利用を禁止する
     */
    @EventHandler
    public void onGuardCraftingWithCustomItem(CraftItemEvent e) {
        final var hasLoreInMatrix = Arrays.stream(e.getInventory().getMatrix())
                .filter(Objects::nonNull)
                .map(item -> item.getItemMeta().lore())
                .anyMatch(l -> l != null && l.size() > 0);

        if (hasLoreInMatrix) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player player) {
                Gui.getInstance().error(player, "§cカスタムアイテムをクラフティングに使用することはできません。");
            }
        }
    }

    /**
     * 焼石製造機を丸石製造機にする
     * 採掘EPボーナスの自動化対策
     */
    @EventHandler
    public void onGuardCobbleStoneGenerator(BlockFormEvent e) {
        if (e.getNewState().getType() == Material.STONE) {
            e.getNewState().setType(Material.COBBLESTONE);
        }
    }

    /**
     * イベントマップでのダメージおよび死を防ぐ
     */
    @EventHandler
    public void onHurtInEventMap(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final Player player)) return;
        if ("event".equals(player.getWorld().getName())) {
            e.setCancelled(true);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                final var loc = player.getWorld().getSpawnLocation();
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    /**
     * イベント期間中は眠れないように
     */
    @EventHandler
    public void onPlayerBed(PlayerBedEnterEvent e) {
        if (EventUtility.isEventNow() && "main".equals(e.getPlayer().getWorld().getName())) {
            e.setUseBed(Event.Result.DENY);
            e.getPlayer().sendMessage(ChatColor.RED + "お祭りムードに溢れている。こんなテンションじゃ寝られない！");
        }
    }

    /**
     * イベントマップ：TA開始イベント
     */
    @EventHandler
    public void onCounterStart(PlayerCounterStart e) {
        final var player = e.getPlayer();
        if (!"event".equals(player.getWorld().getName())) return;

        NbsModule.playRadio(player, "submerged3", NbsModel.PlaybackMode.LOOP);
    }

    /**
     * イベントマップ：TA終了イベント
     */
    @EventHandler
    public void onCounterFinish(PlayerCounterFinish e) {
        final var player = e.getPlayer();
        if (!"event".equals(player.getWorld().getName())) return;

        NbsModule.stopRadio(player);

        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            player.sendMessage(ChatColor.AQUA + "メインワールドに戻る場合は、X Phoneをお使いください。");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1.2f);
        }, Ticks.from(3));
    }

    /**
     * イベントマップに入ったとき、X Phoneがなければ追加する
     */
    @EventHandler
    public void onEnterEventMap(PlayerTeleportEvent e) {
        final var player = e.getPlayer();
        // イベント発生後はまだテレポートが終わっていなかったりするので、5tickほど遅延させる
        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            if (!"event".equals(player.getWorld().getName())) return;
            CustomItemModule.givePhoneIfNeeded(player);
        }, 5);
    }
}
