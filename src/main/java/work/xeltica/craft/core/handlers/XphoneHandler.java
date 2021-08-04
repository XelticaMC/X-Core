package work.xeltica.craft.core.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        var item = e.getItem();
        if (item == null) return;

        var itemMeta = item.getItemMeta();
        if (itemMeta == null || itemMeta.displayName() == null) return;

        var player = e.getPlayer();

        var phone = store().getItem(ItemStore.ITEM_NAME_XPHONE);

        // 右クリック以外はガード
        if (!List.of(
            Action.RIGHT_CLICK_AIR, 
            Action.RIGHT_CLICK_BLOCK
        ).contains(e.getAction())) return;

        // 古いX Phone
        if (!store().compareCustomItem(item, phone)) {
            var pt = PlainTextComponentSerializer.plainText();
            var itemName = pt.serialize(itemMeta.displayName());
            if (item.getType() == Material.WRITTEN_BOOK && itemName.equals("X Phone")) {
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

        var phone = store().getItem(ItemStore.ITEM_NAME_XPHONE);
        var item = e.getPlayer().getInventory().getItemInMainHand();
        if (item == null) return;

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
        var items = new ArrayList<MenuItem>();
        var worldName = player.getWorld().getName();

        var appPromo = new MenuItem("市民システム", i -> {
            player.performCommand("promo");
        }, Material.NETHER_STAR, null);

        var appSidebar = new MenuItem("サイドバー切り替え", i -> {
            player.performCommand("sb toggle");
        }, Material.FILLED_MAP, null);

        var appOmikuji = new MenuItem("おみくじ", i -> {
            player.performCommand("omikuji");
        }, Material.GOLD_INGOT, null);

        var catMode = PlayerStore.getInstance().open(player).getBoolean(PlayerDataKey.CAT_MODE);
        var appCat = new MenuItem("ネコ語モードを" + (catMode ? "オフ" : "オン") + "にする", i -> {
            player.performCommand("cat " + (catMode ? "off" : "on"));
        }, Material.COD, null, catMode);

        var appBoat = new MenuItem("ボート", i -> {
            player.performCommand("boat");
        }, Material.OAK_BOAT, null);

        var appCart = new MenuItem("トロッコ", i -> {
            player.performCommand("cart");
        }, Material.MINECART, null);

        var appCPrivate = new MenuItem("プライベート保護", i -> {
            player.performCommand("cprivate");
        }, Material.TRIPWIRE_HOOK, null);

        var appCPublic = new MenuItem("パブリック保護", i -> {
            player.performCommand("cpublic");
        }, Material.TRIPWIRE_HOOK, null);

        var appCRemove = new MenuItem("保護削除", i -> {
            player.performCommand("cremove");
        }, Material.TRIPWIRE_HOOK, null);

        var appStore = new MenuItem("エビパワーストア", i -> {
            player.performCommand("epshop");
        }, Material.HEART_OF_THE_SEA, null);

        var appPunishment = new MenuItem("処罰", i -> {
            player.performCommand("report");
        }, Material.BARRIER, null);

        var appHint = new MenuItem("ヒント", i -> {
            player.performCommand("hint");
        }, Material.LIGHT, null);

        var bedrockDisclaimer = new MenuItem("統合版プレイヤーのあなたへ", i -> {
            BedrockDisclaimerUtil.showDisclaimer(player);
        }, Material.BEDROCK, null);

        var appTeleport = new MenuItem("テレポート", i -> {
            openTeleportApp(player);
        }, Material.COMPASS, null);

        var isLiveMode = PlayerStore.getInstance().isLiveMode(player);
        var appLive = new MenuItem("配信モードを" + (isLiveMode ? "オフ" : "オン") + "にする", i -> {
            PlayerStore.getInstance().setLiveMode(player, !isLiveMode);
        }, Material.RED_DYE, null);

        var appGeyserAdvancements = new MenuItem("進捗", i -> {
            player.performCommand("geyser advancements");
        }, Material.BEDROCK, null);

        var appGeyserStatistics = new MenuItem("統計", i -> {
            player.performCommand("geyser advancements");
        }, Material.BEDROCK, null);

        var appGeyserOffhand = new MenuItem("持ち物をオフハンドに移動", i -> {
            player.performCommand("geyser offhand");
        }, Material.BEDROCK, null);

        // #endregion

        var isBedrock = FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        
        items.add(appTeleport);

        switch (worldName) {
            case "main":
            case "wildarea2":
            case "wildarea2_nether":
            case "wildarea2_the_end":
            case "wildarea":
            case "world":
            case "world_nether":
            case "world_the_end":
                items.add(appCPrivate);
                items.add(appCPublic);
                items.add(appCRemove);
                break;
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
        var list = new ArrayList<MenuItem>();
        var currentWorldName = p.getWorld().getName();

        list.add(
            switch (currentWorldName) {
                case "hub" -> new MenuItem("メインロビー", i -> {
                    HubStore.getInstance().teleport(p, HubType.Main, true);
                }, Material.NETHERITE_BLOCK);

                case "hub2" -> new MenuItem("クラシックロビー", i -> {
                    HubStore.getInstance().teleport(p, HubType.Classic, true);
                }, Material.GOLD_BLOCK);

                default -> new MenuItem("ロビー", i -> {
                    p.performCommand("hub");
                }, Material.NETHERITE_BLOCK);
            }
        );

        list.add(new MenuItem("初期スポーン", i -> {
            p.performCommand("respawn");
        }, Material.FIREWORK_ROCKET));

        list.add(new MenuItem("ベッド", i -> {
            p.performCommand("respawn bed");
        }, Material.RED_BED));

        ui().openMenu(p, "テレポート", list);
    }

    private void openTeleportAppPlayer(Player player) {
        ui().openPlayersMenu(player, "誰のところにテレポートしますか？", (target) -> {
            if (!EbiPowerStore.getInstance().tryTake(player, 100)) {
                ui().error(player, "EPが足りないため、テレポートできませんでした。");
                return;
            }
            player.sendMessage("5秒後にテレポートします…。");
            var loc = target.getLocation();
            target.sendMessage(String.format("%sが5秒後にあなたの現在位置にテレポートします。", player.getName()));
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
                player.teleport(loc);
            }, 20 * 5);
        });
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }
}
