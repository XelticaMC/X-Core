package work.xeltica.craft.core.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.wesjd.anvilgui.AnvilGUI;
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
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.SoundPitch;
import work.xeltica.craft.core.stores.ItemStore;

/**
 * UI システムのメインクラス。
 * メニューやダイアログといったプレイヤーにUIを表示し、入力を受け取るAPIを持ちます。
 * @author Xeltica
 */
public class Gui implements Listener {
    /**
     * インスタンスを取得します。
     * @return インスタンス
     */
    public static Gui getInstance() {
        return instance == null ? (instance = new Gui()) : instance;
    }

    /**
     * 内部的に使用するものです。
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * メニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param items メニューのアイテム
     */
    public void openMenu(Player player, String title, MenuItem... items) {
        openMenu(player, title, List.of(items));
    }

    /**
     * メニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param items メニューのアイテム
     */
    public void openMenu(Player player, String title, Collection<MenuItem> items) {
        if (isBedrock(player)) {
            openMenuBedrockImpl(player, title, items.toArray(MenuItem[]::new));
        } else {
            openMenuJavaImpl(player, title, items.toArray(MenuItem[]::new));
        }
    }

    /**
     * ダイアログを開きます。
     * @param player ダイアログを開くプレイヤー
     * @param title ダイアログのタイトル
     * @param content ダイアログに記載する文字列
     */
    public void openDialog(Player player, String title, String content) {
        openDialog(player, title, content, null);
    }

    /**
     * ダイアログを開きます。
     * @param player ダイアログを開くプレイヤー
     * @param title ダイアログのタイトル
     * @param content ダイアログに記載する文字列
     * @param callback UIダイアログのボタンを押したときに発火するイベント
     */
    public void openDialog(Player player, String title, String content, Consumer<DialogEventArgs> callback) {
        openDialog(player, title, content, callback, null);
    }

    /**
     * ダイアログを開きます。
     * @param player ダイアログを開くプレイヤー
     * @param title ダイアログのタイトル
     * @param content ダイアログに記載する文字列
     * @param callback UIダイアログのボタンを押したときに発火するイベント
     * @param okButtonText OKボタンのテキスト
     */
    public void openDialog(Player player, String title, String content, Consumer<DialogEventArgs> callback, String okButtonText) {
        final var okText = okButtonText == null ? "OK" : okButtonText;

        if (isBedrock(player)) {
            openDialogBedrockImpl(player, title, content, callback, okText);
        } else {
            openDialogJavaImpl(player, title, content, callback, okText);
        }
    }

    /**
     * 現在参加中のプレイヤーを選択するメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param onSelect 選択肢に入るプレイヤーを指定
     */
    public void openPlayersMenu(Player player, Consumer<Player> onSelect) {
        openPlayersMenu(player, "プレイヤーを選んでください", onSelect);
    }

    /**
     * 現在参加中のプレイヤーを選択するメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param onSelect 選択肢に入るプレイヤーを指定
     * @param title メニューのタイトルを指定
     */
    public void openPlayersMenu(Player player, String title, Consumer<Player> onSelect) {
        openPlayersMenu(player, title, onSelect, null);
    }

    /**
     * 現在参加中のプレイヤーを選択するメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param onSelect 選択肢に入るプレイヤーを指定
     * @param title メニューのタイトルを指定
     * @param filter プレイヤーのフィルターを指定
     */
    public void openPlayersMenu(Player player, String title, Consumer<Player> onSelect, Predicate<Player> filter) {
        var stream = Bukkit.getOnlinePlayers().stream();
        if (filter != null) {
            stream = stream.filter(filter);
        }
        final var list = stream.map(p -> {
                final var head = ItemStore.getInstance().getPlayerHead(p);
                    p.displayName();
                    final var name = PlainTextComponentSerializer.plainText().serialize(p.displayName());
                return new MenuItem(name, (a) -> {
                    if (onSelect != null) onSelect.accept(p);
                }, head, p);
            })
            .toList();

        openMenu(player, title, list);
    }

    /**
     * Java Editionにてボタンを押下したときに実行される内部コマンドの処理を行います。
     * 直接呼び出さないこと。
     * @param id ID
     */
    public void handleCommand(String id) {
        if (!bookHandlersMap.containsKey(id)) return;
        final var t = bookHandlersMap.get(id);
        t.handler.accept(t.eventArgs);
        bookHandlersMap.remove(id);
    }

    /**
     * ブーリアン値に対応するアイコンを取得します
     * @param flag アイコンとなる値
     * @return 対応するアイコン
     */
    public Material getIconOfFlag(boolean flag) {
        return flag ? Material.LIME_DYE : Material.GRAY_DYE;
    }

    /**
     * エラーをプレイヤーに表示します。
     * @param p エラーを表示させるプレイヤー
     * @param message エラー内容
     * @return 常にtrue。コマンドの返り値に使うことを想定。
     */
    public boolean error(Player p, String message) {
        p.sendMessage(message);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
        return true;
    }

    /**
     * JavaでインベントリをメニューUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングに使用するイベント
    */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final var inv = e.getInventory();
        final var p = e.getWhoClicked();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;
        e.setCancelled(true);

        final var menuItems = invMap.get(inv);
        final var id = e.getRawSlot();

        if (menuItems.length <= id || id < 0) return;
        p.closeInventory();
        final var handler = menuItems[id].getOnClick();
        if (handler != null) handler.accept(menuItems[id]);
    }


    /**
     * JavaでインベントリをメニューUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングに使用するイベント
    */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        final var inv = e.getInventory();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;

        // GC
        invMap.remove(inv);
    }


    /**
     * Javaで本をダイアログUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングするイベントを指定
    */
    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (bookSet.contains(e.getPreviousBookMeta())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("てすてすてすとですとよ");
            Bukkit.getLogger().info("テスト");
        }
    }

    /**
     * プレイヤーの位置でサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     */
    public void playSound(Player player, Sound sound, float volume, SoundPitch pitch) {
        player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch.getPitch());
    }

    /**
     * プレイヤーの位置で指定Tick後にサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     * @param delay Tick
     */
    public void playSoundAfter(Player player, Sound sound, float volume, SoundPitch pitch, int delay) {
        Bukkit.getScheduler().runTaskLater(
                XCorePlugin.getInstance(),
                () -> playSound(player, sound, volume, pitch),
                delay
        );
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
        }).forEach(inv::addItem);

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

    public void openTextInput(Player player, String title, Consumer<String> responseHandler) {
        if (isBedrock(player)) {
            openTextInputBedrockImpl(player, title, responseHandler);
        } else {
            openTextInputJavaImpl(player, title, responseHandler);
        }
    }

    private void openTextInputJavaImpl(Player player, String title, Consumer<String> responseHandler) {
        new AnvilGUI.Builder().title(title).onComplete((p, text) -> {
            responseHandler.accept(text);
            return AnvilGUI.Response.close();
        }).itemLeft(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).plugin(XCorePlugin.getInstance()).open(player);
    }

    private void openTextInputBedrockImpl(Player player, String title, Consumer<String> responseHandler) {
        final var fPlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        fPlayer.sendForm(CustomForm.builder().title(title).input("").responseHandler(responseHandler).build());
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
