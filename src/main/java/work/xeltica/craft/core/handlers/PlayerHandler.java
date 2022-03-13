package work.xeltica.craft.core.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.HubType;
import work.xeltica.craft.core.models.OmikujiScore;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.BossBarStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.NickNameStore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.QuickChatStore;
import work.xeltica.craft.core.utils.BedrockDisclaimerUtil;

/**
 * プレイヤーに関するハンドラーをまとめています。
 * TODO: 機能別に再編
 * @author Xeltica
 */
public class PlayerHandler implements Listener {
    public PlayerHandler(Plugin p) {
        this.plugin = p;
        LuckPermsProvider
            .get()
            .getEventBus()
            .subscribe(XCorePlugin.getInstance(), NodeAddEvent.class, this::onNodeAdd);
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final Player p))
            return;
        if (p.getHealth() - e.getFinalDamage() > 0)
            return;

        final var score = OmikujiStore.getInstance().get(p);
        final var th =
            score == OmikujiScore.Tokudaikichi ? 5 :
            score == OmikujiScore.Daikichi ? 1 : 0;

        if ((int)(Math.random() * 100) >= th) return;

        final var i = p.getInventory();
        final var heldItem = i.getItemInMainHand();
        i.remove(heldItem);
        i.setItemInMainHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        new BukkitRunnable(){
            @Override
            public void run() {
                i.setItemInMainHand(heldItem);
            }
        }.runTaskLater(this.plugin, 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var p = e.getPlayer();

        NickNameStore.getInstance().setNickName(p);

        final var name = PlainTextComponentSerializer.plainText().serialize(p.displayName());
        final var pstore = PlayerStore.getInstance();
        e.joinMessage(Component.text("§a" + name + "§b" + "さんがやってきました"));
        if (!p.hasPlayedBefore()) {
            e.joinMessage(Component.text("§a" + name + "§b" + "が§6§l初参加§rです"));
            pstore.open(p).set(PlayerDataKey.NEWCOMER_TIME, DEFAULT_NEW_COMER_TIME);
            HubStore.getInstance().teleport(p, HubType.NewComer, true);
        }
        final var record = pstore.open(p);

        if (!record.getBoolean(PlayerDataKey.GIVEN_PHONE)) {
            p.getInventory().addItem(ItemStore.getInstance().getItem(ItemStore.ITEM_NAME_XPHONE));
            record.set(PlayerDataKey.GIVEN_PHONE, true);
        }

        HintStore.getInstance().achieve(p, Hint.WELCOME);

        BossBarStore.getInstance().applyAll(p);

        if (PlayerStore.getInstance().isCitizen(p)) {
            HintStore.getInstance().achieve(p, Hint.BE_CITIZEN);
        }

        if (!record.getBoolean(PlayerDataKey.BEDROCK_ACCEPT_DISCLAIMER)) {
            BedrockDisclaimerUtil.showDisclaimerAsync(p);
        }

        p.showTitle(Title.title(
            Component.text("§aXelticaMCへ§6ようこそ！"),
            Component.text("§f詳しくは §b§nhttps://craft.xeltica.work§fを見てね！")
        ));

        if (!pstore.isCitizen(p)) {
            if (!pstore.open(p).has(PlayerDataKey.NEWCOMER_TIME)) {
                p.sendMessage("総プレイ時間が30分を超えたため、§b市民§rへの昇格ができます！");
                p.sendMessage("詳しくは §b/promo§rコマンドを実行してください。");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        final var name = PlainTextComponentSerializer.plainText().serialize(e.getPlayer().displayName());
        e.quitMessage(Component.text("§a" + name + "§b" + "さんがかえりました"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatForCat(AsyncPlayerChatEvent e) {
        // ネコであれば文章をいじる
        if (PlayerStore.getInstance().open(e.getPlayer()).getBoolean(PlayerDataKey.CAT_MODE)) {
            var mes = e.getMessage();
            mes = mes.replace("な", "にゃ");
            mes = mes.replace("ナ", "ニャ");
            mes = mes.replace("ﾅ", "ﾆｬ");
            mes = mes.replace("everyone", "everynyan");
            mes = mes.replace("morning", "mornyan");
            mes = mes.replace("na", "nya");
            mes = mes.replace("EVERYONE", "EVERYNYAN");
            mes = mes.replace("MORNING", "MORNYAN");
            mes = mes.replace("NA", "NYA");
            e.setMessage(mes);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayer草ed(AsyncPlayerChatEvent e) {
        final var logger = Bukkit.getLogger();
        if (e.getMessage().equals("草") || e.getMessage().equalsIgnoreCase("kusa") || e.getMessage().equalsIgnoreCase("w")) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    final var block = e.getPlayer().getLocation().subtract(0, 1, 0).getBlock();
                    if (block.getType() != Material.GRASS_BLOCK) {
                        return;
                    }

                    final var id = e.getPlayer().getUniqueId();
                    final var lastTime = last草edTimeMap.getOrDefault(id, Integer.MIN_VALUE);
                    final var nowTime = Bukkit.getCurrentTick();
                    if (lastTime == Integer.MIN_VALUE || nowTime - lastTime > 20 * 60) {
                        block.applyBoneMeal(BlockFace.UP);
                    }
                    last草edTimeMap.put(id, nowTime);
                    HintStore.getInstance().achieve(e.getPlayer(), Hint.KUSA);
                }
            }.runTask(plugin);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuickChat(AsyncPlayerChatEvent e) {
        var msg = e.getMessage();
        final var store = QuickChatStore.getInstance();
        final var player = e.getPlayer();
        if (!msg.startsWith(".")) return;
        msg = msg.substring(1);
        if (store.getAllPrefix().contains(msg)) {
            var quickMsg = store.getMessage(msg);
            if (quickMsg != null) {
                quickMsg = store.chatFormat(quickMsg, player);
                e.setMessage(quickMsg);
                HintStore.getInstance().achieve(player, Hint.QUICKCHAT);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent e) {
        final var p = e.getPlayer();
        if (p.getWorld().getName().startsWith("wildareab")) {
            var respawnLocation = p.getBedSpawnLocation();
            if (respawnLocation == null) {
                respawnLocation = Bukkit.getWorld("main").getSpawnLocation();
            }
            e.setRespawnLocation(respawnLocation);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        // TODO 旅行券を持つとタイトル表示が発生するようにする
    }

    @EventHandler
    public void onPlayerTryBed(PlayerInteractEvent e) {
        final var p = e.getPlayer();
        final var isSneaking = p.isSneaking();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final var worldName = p.getWorld().getName();
            final var isBedDisabledWorld = worldName.equals("hub2")
                || worldName.equals("sandbox")
                || worldName.equals("wildareab")
                ;
            if (isBedDisabledWorld && Tag.BEDS.isTagged(Objects.requireNonNull(e.getClickedBlock()).getType())) {
                Gui.getInstance().error(p, "ベッドはこの世界では使えない…");
                e.setCancelled(true);
            }
        }
    }

    private void onNodeAdd(NodeAddEvent e) {
        if (!e.isUser()) return;
        final var target = (User)e.getTarget();
        final var node = e.getNode();

        Bukkit.getScheduler().runTask(XCorePlugin.getInstance(), () -> {
            final Player player = Bukkit.getPlayer(target.getUniqueId());
            if (player == null) return;

            if (node instanceof InheritanceNode in && "citizen".equals(in.getGroupName())) {
                HintStore.getInstance().achieve(player, Hint.BE_CITIZEN);
            }
        });
    }

    private final Plugin plugin;
    private final Random rnd = new Random();
    private UUID movingPlayer = null;
    private final Map<UUID, Integer> last草edTimeMap = new HashMap<>();

    // 30分
    private final int DEFAULT_NEW_COMER_TIME = 20 * 60 * 30;
}
