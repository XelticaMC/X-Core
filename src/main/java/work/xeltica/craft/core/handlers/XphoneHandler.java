package work.xeltica.craft.core.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.WorldStore;
import work.xeltica.craft.core.utils.BedrockDisclaimerUtil;

/**
 * X Phoneに関する機能をまとめています。
 * @author Xeltica
 */
public class XphoneHandler implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        final var item = e.getItem();
        if (item == null) return;

        final var itemMeta = item.getItemMeta();
        if (itemMeta == null || itemMeta.displayName() == null) return;

        final var player = e.getPlayer();

        final var phone = store().getItem(ItemStore.ITEM_NAME_XPHONE);

        // 右クリック以外はガード
        if (!List.of(
            Action.RIGHT_CLICK_AIR,
            Action.RIGHT_CLICK_BLOCK
        ).contains(e.getAction())) return;

        // 古いX Phone
        if (!store().compareCustomItem(item, phone)) {
            final var pt = PlainTextComponentSerializer.plainText();
            final var displayName = itemMeta.displayName();
            final var itemName = displayName == null ? null : pt.serialize(displayName);
            if (item.getType() == Material.WRITTEN_BOOK && "X Phone".equals(itemName)) {
                e.setUseItemInHand(Result.DENY);
                player.getInventory().remove(item);
                player.getInventory().addItem(phone);
            }
            return;
        }

        e.setUseItemInHand(Result.DENY);

        openSpringBoard(player);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onOffhandUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.OFF_HAND) return;

        final var phone = store().getItem(ItemStore.ITEM_NAME_XPHONE);
        final var item = e.getPlayer().getInventory().getItemInMainHand();

        // 右クリック以外はガード
        if (!List.of(
            Action.RIGHT_CLICK_AIR,
            Action.RIGHT_CLICK_BLOCK
        ).contains(e.getAction())) return;

        // メインハンドがX Phoneであればオフハンドも使用停止
        if (store().compareCustomItem(item, phone)) {
            e.setUseItemInHand(Result.DENY);
        }
    }

    private void openSpringBoard(@NotNull Player player) {
        final var items = new ArrayList<MenuItem>();
        final var catMode = PlayerStore.getInstance().open(player).getBoolean(PlayerDataKey.CAT_MODE);
        final var isLiveMode = PlayerStore.getInstance().isLiveMode(player);
        final var worldName = player.getWorld().getName();

        final var appPromo = new MenuItem("市民システム", i -> player.performCommand("promo"), Material.NETHER_STAR, null);
        final var appSidebar = new MenuItem("サイドバー切り替え", i -> player.performCommand("sb toggle"), Material.FILLED_MAP, null);
        final var appOmikuji = new MenuItem("おみくじ", i -> player.performCommand("omikuji"), Material.GOLD_INGOT, null);
        final var appCat = new MenuItem("ネコ語モードを" + (catMode ? "オフ" : "オン") + "にする", i -> player.performCommand("cat " + (catMode ? "off" : "on")), Material.COD, null, catMode);
        final var appBoat = new MenuItem("ボート", i -> player.performCommand("boat"), Material.OAK_BOAT, null);
        final var appCart = new MenuItem("トロッコ", i -> player.performCommand("cart"), Material.MINECART, null);

        final var appFirework = new MenuItem("花火を購入（80EP/5個）", i -> {
            if (!EbiPowerStore.getInstance().tryTake(player, 80)) {
                ui().error(player, "アイテムを購入できませんでした。エビパワーが足りません。");
                return;
            }
            final var size = player.getInventory().addItem(PlayerStore.getInstance().getRandomFireworkByUUID(player.getUniqueId(), 5)).size();
            if (size > 0) {
                ui().error(player, "アイテムを購入できませんでした。持ち物がいっぱいです。整理してからもう一度お試し下さい。");
                EbiPowerStore.getInstance().tryGive(player, 80);
            } else {
                player.sendMessage("花火を購入しました！");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 2);
            }
        }, Material.FIREWORK_ROCKET, null, true);

        final var appCPrivate = new MenuItem("プライベート保護", i -> player.performCommand("cprivate"), Material.TRIPWIRE_HOOK, null);
        final var appCPublic = new MenuItem("パブリック保護", i -> player.performCommand("cpublic"), Material.TRIPWIRE_HOOK, null);
        final var appCRemove = new MenuItem("保護削除", i -> player.performCommand("cremove"), Material.TRIPWIRE_HOOK, null);
        final var appStore = new MenuItem("エビパワーストア", i -> player.performCommand("epshop"), Material.HEART_OF_THE_SEA, null);
        final var appPunishment = new MenuItem("処罰", i -> player.performCommand("report"), Material.BARRIER, null);
        final var appHint = new MenuItem("ヒント", i -> player.performCommand("hint"), Material.LIGHT, null);
        final var bedrockDisclaimer = new MenuItem("統合版プレイヤーのあなたへ", i -> BedrockDisclaimerUtil.showDisclaimer(player), Material.BEDROCK, null);
        final var appTeleport = new MenuItem("テレポート", i -> openTeleportApp(player), Material.COMPASS, null);
        final var appLive = new MenuItem("配信モードを" + (isLiveMode ? "オフ" : "オン") + "にする", i -> PlayerStore.getInstance().setLiveMode(player, !isLiveMode), Material.RED_DYE, null);
        final var appGeyserAdvancements = new MenuItem("進捗", i -> player.performCommand("geyser advancements"), Material.BEDROCK, null);
        final var appGeyserStatistics = new MenuItem("統計", i -> player.performCommand("geyser advancements"), Material.BEDROCK, null);
        final var appGeyserOffhand = new MenuItem("持ち物をオフハンドに移動", i -> player.performCommand("geyser offhand"), Material.BEDROCK, null);

        final var isBedrock = FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());

        items.add(appTeleport);

        if (List.of("main", "wildarea2", "wildarea2_nether", "wildarea2_the_end", "wildarea", "world", "world_nether", "world_the_end").contains(worldName)) {
            items.add(appCPrivate);
            items.add(appCPublic);
            items.add(appCRemove);
        }

        if ("main".equals(worldName)) {
            items.add(appFirework);
        }

        if (isBedrock) {
            items.add(appGeyserAdvancements);
            items.add(appGeyserStatistics);
            items.add(appGeyserOffhand);
        }

        items.add(appPromo);
        items.add(appSidebar);
        items.add(appOmikuji);
        items.add(appCat);
        if (WorldStore.getInstance().canSummonVehicles(player.getWorld())) {
            items.add(appBoat);
            items.add(appCart);
        }
        items.add(appStore);

        items.add(appHint);

        items.add(appLive);

        if (isBedrock) {
            items.add(bedrockDisclaimer);
        }

        ui().openMenu(player, "X Phone OS", items);
    }

    private void openTeleportApp(Player p) {
        final var list = new ArrayList<MenuItem>();
        final var currentWorldName = p.getWorld().getName();

        list.add(
            switch (currentWorldName) {
                case "hub" -> new MenuItem("メインロビー", i -> HubStore.getInstance().teleport(p, HubType.Main, true), Material.NETHERITE_BLOCK);
                case "hub2" -> new MenuItem("クラシックロビー", i -> HubStore.getInstance().teleport(p, HubType.Classic, true), Material.GOLD_BLOCK);
                default -> new MenuItem("ロビー", i -> p.performCommand("hub"), Material.NETHERITE_BLOCK);
            }
        );

        list.add(new MenuItem("初期スポーン", i -> p.performCommand("respawn"), Material.FIREWORK_ROCKET));
        list.add(new MenuItem("ベッド", i -> p.performCommand("respawn bed"), Material.RED_BED));

        ui().openMenu(p, "テレポート", list);
    }

    private void openTeleportAppPlayer(Player player) {
        ui().openPlayersMenu(player, "誰のところにテレポートしますか？", (target) -> {
            if (!EbiPowerStore.getInstance().tryTake(player, 100)) {
                ui().error(player, "EPが足りないため、テレポートできませんでした。");
                return;
            }
            player.sendMessage("5秒後にテレポートします…。");
            final var loc = target.getLocation();
            target.sendMessage(String.format("%sが5秒後にあなたの現在位置にテレポートします。", player.getName()));
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> player.teleport(loc), 20 * 5);
        });
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }
}
