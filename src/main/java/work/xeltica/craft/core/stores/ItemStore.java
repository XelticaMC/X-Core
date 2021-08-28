package work.xeltica.craft.core.stores;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * カスタムアイテムを管理します。
 * TODO: 近いうちにカスタムアイテムの管理方法を大きく変更する予定です…
 * @author Xeltica
 */
public class ItemStore {
    public static final String ITEM_NAME_XPHONE = "xphone";
    public static final String ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT = "ticket_wildareab_ocean_monument";

    public ItemStore() {
        ItemStore.instance = this;
        registerItems();
        Bukkit.getOnlinePlayers().forEach(this::givePhoneIfNeeded);
    }

    public static ItemStore getInstance() {
        return instance;
    }

    public ItemStack getItem(String key) {
        return customItems.get(key).clone();
    }

    /**
     * カスタムアイテムを作成
     */
    public ItemStack createCustomItem(String name, String... lore) {
        final var st = new ItemStack(Material.KNOWLEDGE_BOOK);

        st.editMeta(meta -> {
            meta.displayName(
                Component.text(name).style(Style.style(TextColor.color(37, 113, 255), TextDecoration.BOLD))
            );
            meta.lore(Stream.of(lore).map(s -> Component.text(s).asComponent()).toList());
        });
        return st;
    }

    /**
     * カスタムアイテムの比較
     */
    public boolean compareCustomItem(ItemStack stack1, ItemStack stack2) {
        // -- どっちもnull
        if (stack1 == null && stack2 == null) return true;

        // -- どっちかがnull
        if (stack1 == null || stack2 == null) return false;

        // -- 種類が違う
        if (stack1.getType() != stack2.getType()) return false;

        final var meta1 = stack1.getItemMeta();
        final var meta2 = stack2.getItemMeta();

        // -- 名前の比較
        final var name1 = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta1.displayName()));
        final var name2 = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta2.displayName()));
        if (!name1.equals(name2)) return false;

        // -- lore の比較
        final var lore1 = Objects.requireNonNull(meta1.lore()).stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        final var lore2 = Objects.requireNonNull(meta2.lore()).stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        return lore1.equals(lore2);
    }

    public void givePhoneIfNeeded(@NotNull Player player) {
        final var inv = player.getInventory();
        final var phone = getItem(ItemStore.ITEM_NAME_XPHONE);
        final var hasItem = Streams.stream(inv)
            .anyMatch(a -> compareCustomItem(a, getItem(ItemStore.ITEM_NAME_XPHONE)));
        if (!hasItem) inv.addItem(phone);
    }

    public ItemStack getPlayerHead(Player player) {
        final var stack = new ItemStack(Material.PLAYER_HEAD);

        stack.editMeta(m -> {
            if (m instanceof SkullMeta skullMeta) {
                resolvePlayerHeadWithBukkit(player, skullMeta);
            }
        });
        return stack;
    }

    // private void resolvePlayerHeadWithSkinsRestorer(Player player, SkullMeta meta) {
    //     var sapi = SkinsRestorerAPI.getApi();

    //     var skinName = sapi.getSkinName(player.getName());
    //     if (skinName == null) {
    //         resolvePlayerHeadWithBukkit(player, meta);
    //         return;
    //     }
    //     var skin = sapi.getSkinData(skinName);

    //     var profile = Bukkit.createProfile(skin.getName());
    //     profile.setProperty(new ProfileProperty("textures", skin.getValue(), skin.getSignature()));
    //     meta.setPlayerProfile(profile);
    // }

    private void resolvePlayerHeadWithBukkit(Player player, SkullMeta meta) {
        meta.setPlayerProfile(player.getPlayerProfile());
    }

    private void registerItems() {
        customItems.put(ITEM_NAME_XPHONE, createCustomItem("X Phone SE", "XelticaMCの独自機能にアクセスできるスマホ。"));
        customItems.put(ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT, createCustomItem("海底神殿行きワイルドエリアB旅行券", "メイン ✈ 海底神殿"));
    }

    private final Map<String, ItemStack> customItems = new HashMap<>();
    private static ItemStore instance;
}
