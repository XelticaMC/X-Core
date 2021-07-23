package work.xeltica.craft.core.handlers;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.utils.BedrockDisclaimerUtil;

public class XphoneHandler implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        var item = e.getItem();
        if (item == null) return;

        var itemMeta = item.getItemMeta();
        if (itemMeta == null || itemMeta.displayName() == null) return;

        var player = e.getPlayer();

        var phone = store().getItem(ItemStore.ITEM_NAME_XPHONE);

        if (!store().compareCustomItem(item, phone)) {
            var pt = PlainTextComponentSerializer.plainText();
            var itemName = pt.serialize(itemMeta.displayName());
            var phoneName = pt.serialize(phone.getItemMeta().displayName());
            if (item.getType() == Material.WRITTEN_BOOK && phoneName.equals(itemName)) {
                player.sendMessage("古いX Phoneは使えなくなりました。捨てた上で /xphone コマンドを実行して入手してください。");
                e.setUseItemInHand(Result.DENY);
            }
            return;
        }

        if (e.getAction() == Action.PHYSICAL) return;

        e.setUseItemInHand(Result.DENY);

        openSpringBoard(player);
    }

    private void openSpringBoard(@NotNull Player player) {
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
            case "wildarea":
            case "wildarea_nether":
            case "wildarea_the_end":
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

        items.add(appHint);

        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            items.add(bedrockDisclaimer);
        }

        ui().openMenu(player, "X Phone OS", items);
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }
}
