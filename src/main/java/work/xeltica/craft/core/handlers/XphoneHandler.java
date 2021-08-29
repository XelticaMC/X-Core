package work.xeltica.craft.core.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.QuickChatStore;
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
        if (!List.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(e.getAction())) return;

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
        final var summerLoginBonusReceived = PlayerStore.getInstance().open(player).getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER);

        final var appPromo = new MenuItem("市民システム", i -> player.performCommand("promo"), Material.NETHER_STAR, null);
        final var appSidebar = new MenuItem("サイドバー切り替え", i -> player.performCommand("sb toggle"), Material.FILLED_MAP, null);
        final var appOmikuji = new MenuItem("おみくじ", i -> player.performCommand("omikuji"), Material.GOLD_INGOT, null);
        final var appCat = new MenuItem("ネコ語モードを" + (catMode ? "オフ" : "オン") + "にする", i -> player.performCommand("cat " + (catMode ? "off" : "on")), Material.COD, null, catMode);
        final var appBoat = new MenuItem("ボート", i -> player.performCommand("boat"), Material.OAK_BOAT, null);
        final var appCart = new MenuItem("トロッコ", i -> player.performCommand("cart"), Material.MINECART, null);

        final var appFirework = new MenuItem(summerLoginBonusReceived ? "花火を購入（80EP/5個）" : "花火を引き換える", i -> {
            final var verb = summerLoginBonusReceived ? "購入" : "入手";
            if (summerLoginBonusReceived && !EbiPowerStore.getInstance().tryTake(player, 80)) {
                ui().error(player, "アイテムを" + verb + "できませんでした。エビパワーが足りません。");
                return;
            }
            final var stack = PlayerStore.getInstance().getRandomFireworkByUUID(player.getUniqueId(), 5);
            final var size = player.getInventory().addItem(stack).size();
            if (size > 0) {
                ui().error(player, "アイテムを" + verb + "できませんでした。持ち物がいっぱいです。整理してからもう一度お試し下さい。");
                if (size - 5 > 0) {
                    final var stackToRemove = stack.clone();
                    stackToRemove.setAmount(size - 5);
                    player.getInventory().remove(stackToRemove);
                }
                EbiPowerStore.getInstance().tryGive(player, 80);
            } else {
                player.sendMessage("花火を" + verb + "しました！");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 2);

                // 購入の場合、ヒント達成する
                if (summerLoginBonusReceived) {
                    HintStore.getInstance().achieve(player, Hint.BUY_FIREWORKS);
                }
            }
            if (!summerLoginBonusReceived) {
                PlayerStore.getInstance().open(player).set(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER, true);
            }
        }, Material.FIREWORK_ROCKET, null, !summerLoginBonusReceived);

        final var appCPrivate = new MenuItem("プライベート保護", i -> player.performCommand("cprivate"), Material.TRIPWIRE_HOOK, null);
        final var appCPublic = new MenuItem("パブリック保護", i -> player.performCommand("cpublic"), Material.TRIPWIRE_HOOK, null);
        final var appCRemove = new MenuItem("保護削除", i -> player.performCommand("cremove"), Material.TRIPWIRE_HOOK, null);
        final var appStore = new MenuItem("エビパワーストア", i -> player.performCommand("epshop"), Material.HEART_OF_THE_SEA, null);
        // TODO: 処罰システムを改良したら実装する
        final var appPunishment = new MenuItem("処罰", i -> player.performCommand("report"), Material.BARRIER, null);
        final var appHint = new MenuItem("ヒント", i -> player.performCommand("hint"), Material.LIGHT, null);
        final var bedrockDisclaimer = new MenuItem("統合版プレイヤーのあなたへ", i -> BedrockDisclaimerUtil.showDisclaimer(player), Material.BEDROCK, null);
        final var appTeleport = new MenuItem("テレポート", i -> openTeleportApp(player), Material.COMPASS, null);
        final var appLive = new MenuItem("配信モードを" + (isLiveMode ? "オフ" : "オン") + "にする", i -> PlayerStore.getInstance().setLiveMode(player, !isLiveMode), Material.RED_DYE, null);
        final var appGeyserAdvancements = new MenuItem("進捗", i -> player.performCommand("geyser advancements"), Material.BEDROCK, null);
        final var appGeyserStatistics = new MenuItem("統計", i -> player.performCommand("geyser advancements"), Material.BEDROCK, null);
        final var appGeyserOffhand = new MenuItem("持ち物をオフハンドに移動", i -> player.performCommand("geyser offhand"), Material.BEDROCK, null);
        final var appQuickChat = new MenuItem("クイックチャット", i -> openQuickChatApp(player), Material.PAPER, null);
        final var launchFireworkApp = new MenuItem("花火を打ち上げる", i -> openFireworkLaunchApp(player), Material.FIREWORK_ROCKET, null);

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

        items.add(appQuickChat);
        if (player.hasPermission("otanoshimi.app.fireworks")) {
            items.add(launchFireworkApp);
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

        if ("main".equals(currentWorldName)) {
            list.add(new MenuItem("ワイルドエリアBへ行く", i -> {
                final var loc = p.getLocation();
                final var x = loc.getBlockX() * 16;
                final var z = loc.getBlockZ() * 16;
                p.sendMessage("ワールドを準備中です…");
                p.getWorld().getChunkAtAsync(x, z).thenAccept((c) -> {
                    final var wildareab = Bukkit.getWorld("wildareab");
                    if (wildareab == null) {
                        ui().error(p, "テレポートに失敗しました。ワールドが作成されていないようです。");
                        return;
                    }
                    final var y = wildareab.getHighestBlockYAt(x, z) + 1;
                    final var land = new Location(wildareab, x, y - 1, z);
                    if (land.getBlock().getType() == Material.WATER) {
                        land.getBlock().setType(Material.STONE);
                    }
                    p.teleportAsync(new Location(wildareab, x, y, z));
                });
            }, Material.GRASS_BLOCK));
        } else if ("wildareab".contains(currentWorldName)) {
            list.add(new MenuItem("メインワールドに帰る", i -> {
                WorldStore.getInstance().teleportToSavedLocation(p, "main");
            }, Material.CREEPER_HEAD));
        }

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
            Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> player.teleportAsync(loc), 20 * 5);
        });
    }

    private void openQuickChatApp(Player player) {
        final var store = QuickChatStore.getInstance();
        final var list = new ArrayList<MenuItem>();

        for (String chat : store.getAllPrefix()) {
            final var msg = store.chatFormat(store.getMessage(chat), player);
            list.add(new MenuItem(String.format("%s §7(.%s)", msg, chat), i -> {
                player.chat(msg);
                HintStore.getInstance().achieve(player, Hint.QUICKCHAT_APP);
            }, Material.PAPER));
        }

        ui().openMenu(player, "QuickChat", list);
    }

    private void openFireworkLaunchApp(Player player) {
        final var list = new ArrayList<MenuItem>();

        for (FireworkType fireworkType: FireworkType.values()) {
            list.add(new MenuItem(fireworkType.type.name(), i -> chooseFireworkColor(player, fireworkType.type), fireworkType.material));
        }

        ui().openMenu(player, "Launch Firework", list);
    }

    private void chooseFireworkColor(Player player, FireworkEffect.Type type) {
        final var list = new ArrayList<MenuItem>();

        for (FireworkColor fireworkColor: FireworkColor.values()) {
            list.add(new MenuItem(fireworkColor.name(), i -> spawnFirework(player, type, fireworkColor.color), fireworkColor.material));
        }

        ui().openMenu(player, "Launch Firework", list);
    }

    private void spawnFirework(Player player, FireworkEffect.Type type, Color color) {
        final var firework = player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        if (firework instanceof Firework) {
            final var meta = ((Firework) firework).getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(type).withColor(color).build());
            ((Firework) firework).setFireworkMeta(meta);
        }
    }

    private ItemStore store() { return ItemStore.getInstance(); }
    private Gui ui() { return Gui.getInstance(); }

    enum FireworkType {
        SMALL(FireworkEffect.Type.BALL,Material.FIRE_CHARGE),
        LARGE(FireworkEffect.Type.BALL_LARGE,Material.FIRE_CHARGE),
        STAR(FireworkEffect.Type.STAR,Material.NETHER_STAR),
        BURST(FireworkEffect.Type.BURST,Material.TNT),
        CREEPER(FireworkEffect.Type.CREEPER ,Material.CREEPER_HEAD);

        FireworkType(FireworkEffect.Type type, Material material) {
            this.type = type;
            this.material = material;
        }

        private final FireworkEffect.Type type;
        private final Material material;
    }

    enum FireworkColor {
        ORANGE(Material.ORANGE_WOOL, Color.ORANGE),
        FUCHSIA(Material.MAGENTA_WOOL, Color.FUCHSIA),
        LIGHTBLUE(Material.LIGHT_BLUE_WOOL, Color.AQUA),
        YELLOW(Material.YELLOW_WOOL, Color.YELLOW),
        LIME(Material.LIME_WOOL, Color.LIME),
        PINK(Material.PINK_WOOL, Color.fromRGB(0xff5c84)),
        GRAY(Material.GRAY_WOOL, Color.GRAY),
        LIGHTGRAY(Material.LIGHT_GRAY_WOOL, Color.fromRGB(0x808080)),
        CYAN(Material.CYAN_WOOL, Color.fromRGB(0x43fcf3)),
        PURPLE(Material.PURPLE_WOOL, Color.PURPLE),
        BLUE(Material.BLUE_WOOL, Color.BLUE),
        BROWN(Material.BROWN_WOOL, Color.fromRGB(0x734e30)),
        GREEN(Material.GREEN_WOOL, Color.GREEN),
        RED(Material.RED_WOOL, Color.RED),
        BLACK(Material.BLACK_WOOL, Color.BLACK);

        FireworkColor(Material material ,Color color) {
            this.material = material;
            this.color = color;
        }

        private final Material material;
        private final Color color;
    }
}
