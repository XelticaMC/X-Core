package work.xeltica.craft.core.handlers;

import java.util.ArrayList;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.geysermc.floodgate.api.FloodgateApi;

import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.utils.BedrockDisclaimerUtil;

public class XphoneHandler implements Listener {
    @EventHandler
    public void on(PlayerChangedWorldEvent e) {
        store().givePhoneIfNeeded(e.getPlayer());
    }

    @EventHandler
    public void on(PlayerPostRespawnEvent e) {
        store().givePhoneIfNeeded(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        var item = e.getPlayer().getInventory().getItem(e.getHand());
        var player = e.getPlayer();
        if (!store().compareCustomItem(item, store().getItem(ItemStore.ITEM_NAME_XPHONE))) return;
        if (e.getAction() != Action.LEFT_CLICK_AIR) {
            e.setCancelled(true);
            return;
        };
        e.setCancelled(true);

        var items = new ArrayList<MenuItem>();
        var worldName = player.getWorld().getName();

        // #region App初期化
        var appClassicHub = new MenuItem("クラシックロビーへ", i -> {
            HubStore.getInstance().teleport(player, HubType.Classic, true);
        }, Material.GOLD_BLOCK);
        var appMainHub = new MenuItem("メインロビーへ", i -> {
            HubStore.getInstance().teleport(player, HubType.Main, true);
        }, Material.NETHERITE_BLOCK);
        var appHub = new MenuItem("ロビーへ", i -> {
            player.performCommand("hub");
        }, Material.NETHERITE_BLOCK);
        var appRespawn = new MenuItem("初期スポーンへ", i -> {
            player.performCommand("respawn");
        }, Material.FIREWORK_ROCKET);
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
        // #endregion

        if (worldName.equals("hub2")) {
            items.add(appClassicHub);
        } else if (worldName.equals("hub")) {
            items.add(appMainHub);
        } else {
            items.add(appHub);
        }

        switch (worldName) {
            case "main":
            case "world":
            case "world_nether":
            case "world_the_end":
                items.add(appCPrivate);
                items.add(appCPublic);
                items.add(appCRemove);
                break;
        }
        
        items.add(appRespawn);
        items.add(appPromo);
        items.add(appSidebar);
        items.add(appOmikuji);
        items.add(appCat);
        items.add(appBoat);
        items.add(appCart);
        items.add(appStore);

        // TODO 通報機能のリニューアルをしたら開放
        // if (e.getPlayer().hasPermission("otanoshimi.command.report")) {
        //     items.add(appPunishment);
        // }

        items.add(appHint);

        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            items.add(bedrockDisclaimer);
        }

        ui().openMenu(e.getPlayer(), "X Phone OS", items);
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }
}
