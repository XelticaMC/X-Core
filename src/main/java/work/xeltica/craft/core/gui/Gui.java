package work.xeltica.craft.core.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.geysermc.connector.common.ChatColor;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.stores.ItemStore;

/**
 * UI システムのメインクラス。
 * メニューやダイアログといったプレイヤーにUIを表示し、入力を受け取るAPIを持ちます。
 * @author Xeltica
 */
public class Gui implements Listener {
    public static Gui getInstance() {
        return instance == null ? (instance = new Gui()) : instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void openMenu(Player player, String title, MenuItem... items) {
        openMenu(player, title, List.of(items));
    }

    public void openMenu(Player player, String title, Collection<MenuItem> items) {
        if (isBedrock(player)) {
            openMenuBedrockImpl(player, title, items.toArray(MenuItem[]::new));
        } else {
            openMenuJavaImpl(player, title, items.toArray(MenuItem[]::new));
        }
    }

    public void openDialog(Player player, String title, String content) {
        openDialog(player, title, content, null);
    }

    public void openDialog(Player player, String title, String content, Consumer<DialogEventArgs> callback) {
        openDialog(player, title, content, callback, null);
    }

    public void openDialog(Player player, String title, String content, Consumer<DialogEventArgs> callback, String okButtonText) {
        final var okText = okButtonText == null ? "OK" : okButtonText;

        if (isBedrock(player)) {
            openDialogBedrockImpl(player, title, content, callback, okText);
        } else {
            openDialogJavaImpl(player, title, content, callback, okText);
        }
    }

    public void openPlayersMenu(Player player, Consumer<Player> onSelect) {
        openPlayersMenu(player, "プレイヤーを選んでください", onSelect);
    }

    public void openPlayersMenu(Player player, String title, Consumer<Player> onSelect) {
        openPlayersMenu(player, title, onSelect, null);
    }

    public void openPlayersMenu(Player player, String title, Consumer<Player> onSelect, Predicate<Player> filter) {
        var stream = Bukkit.getOnlinePlayers().stream();
        if (filter != null) {
            stream = stream.filter(filter);
        }
        final var list = stream.map(p -> {
                final var head = ItemStore.getInstance().getPlayerHead(p);
                final var name = p.displayName() != null ? PlainTextComponentSerializer.plainText().serialize(p.displayName()) : p.getName();
                return new MenuItem(name, (a) -> {
                    if (onSelect != null) onSelect.accept(p);
                }, head, p);
            })
            .toList();

        openMenu(player, title, list);
    }

    public void handleCommand(String id) {
        if (!bookHandlersMap.containsKey(id)) return;
        final var t = bookHandlersMap.get(id);
        t.handler.accept(t.eventArgs);
        bookHandlersMap.remove(id);
        return;
    }

    /**
     * エラーをプレイヤーに表示します。
     * @return 常にtrue。コマンドの返り値に使うことを想定。
     */
    public boolean error(Player p, String message) {
        p.sendMessage(message);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final var inv = e.getInventory();
        final var p = e.getWhoClicked();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;
        e.setCancelled(true);

        final var menuItems = invMap.get(inv);
        final var id = e.getRawSlot();

        if (menuItems.length <= id || menuItems.length < 0) return;
        p.closeInventory();
        final var handler = menuItems[id].getOnClick();
        if (handler != null) handler.accept(menuItems[id]);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        final var inv = e.getInventory();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;

        // GC
        invMap.remove(inv);
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (bookSet.contains(e.getPreviousBookMeta())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("てすてすてすとですとよ");
            Bukkit.getLogger().info("テスト");
        }
    }

    private void openMenuJavaImpl(Player player, String title, MenuItem[] items) {
        final var inv = Bukkit.createInventory(null, (1 + items.length / 9) * 9, Component.text(title));

        Arrays.stream(items).map(i -> {
            final var item = i.getIcon();
            if (i.isShiny()) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            }
            final var meta = item.getItemMeta();
            meta.displayName(Component.text(i.getName()));
            item.setItemMeta(meta);

            return item;
        }).forEach(i -> inv.addItem(i));

        invMap.put(inv, items);
        player.openInventory(inv);
    }

    private void openMenuBedrockImpl(Player player, String title, MenuItem[] items) {
        final var builder = SimpleForm.builder()
            .title(title);

        for (var item : items) {
            var text = item.getName();
            if (item.isShiny()) {
                text = ChatColor.GREEN + text;
            }
            builder.button(text);
        }

        builder.responseHandler((form, data) -> {
            final var res = form.parseResponse(data);
            if (!res.isCorrect()) {
                return;
            }

            final var id = res.getClickedButtonId();
            final var callback = items[id].getOnClick();
            if (callback != null) {
                callback.accept(items[id]);
            }
        });

        final var fPlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        fPlayer.sendForm(builder);
    }

    private void openDialogJavaImpl(Player player, String title, String content, Consumer<DialogEventArgs> callback, String okButtonText) {
        final var book = new ItemStack(Material.WRITTEN_BOOK);
        final var meta = (BookMeta)book.getItemMeta();

        final var handleString = UUID.randomUUID().toString().replace("-", "");

        final var comTitle = Component.text(title + "\n\n", Style.style(TextDecoration.BOLD));
        final var comContent = Component.text(content + "\n\n");
        final var comOkButton = Component.text(okButtonText, Style.style(TextColor.color(0, 0, 0), TextDecoration.BOLD, TextDecoration.UNDERLINED))
            .clickEvent(ClickEvent.runCommand("/__core_gui_event__ " + handleString));

        final var component = comTitle
            .append(comContent)
            .append(comOkButton)
            ;

        meta.addPages(component);
        meta.setAuthor("XelticaMC");
        meta.setTitle(title);

        book.setItemMeta(meta);

        bookSet.add(meta);

        player.openBook(book);

        if (callback != null) {
            bookHandlersMap.put(handleString, new HandlerTuple(callback, new DialogEventArgs(player), meta));
        }
    }

    private void openDialogBedrockImpl(Player player, String title, String content,
            Consumer<DialogEventArgs> callback, String okButtonText) {
        final var api = FloodgateApi.getInstance();
        final var form = SimpleForm.builder()
            .title(title)
            .content(content)
            .button(okButtonText)
            .responseHandler(r -> {
                if (callback != null) {
                    callback.accept(new DialogEventArgs(player));
                }
            });
        api.getPlayer(player.getUniqueId()).sendForm(form);
    }

    private static boolean isBedrock(Player player) {
        return FloodgateApi.getInstance().isFloodgateId(player.getUniqueId());
    }

    private final HashMap<Inventory, MenuItem[]> invMap = new HashMap<>();
    private final HashMap<String, HandlerTuple> bookHandlersMap = new HashMap<>();
    private final HashSet<BookMeta> bookSet = new HashSet<>();
    private static Gui instance;

    class HandlerTuple {
        public Consumer<DialogEventArgs> handler;
        public BookMeta meta;
        public DialogEventArgs eventArgs;

        public HandlerTuple(Consumer<DialogEventArgs> handler, DialogEventArgs eventArgs, BookMeta meta) {
            this.handler = handler;
            this.eventArgs = eventArgs;
            this.meta = meta;
        }
    }
}
