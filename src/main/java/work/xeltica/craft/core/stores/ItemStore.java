package work.xeltica.craft.core.stores;

import java.util.HashMap;
import java.util.Map;
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

public class ItemStore {
    public static final String ITEM_NAME_XPHONE = "xphone";

    public ItemStore() {
        ItemStore.instance = this;
        registerItems();
        Bukkit.getOnlinePlayers().forEach(p -> givePhoneIfNeeded(p));
    }

    public static ItemStore getInstance() {
        return instance;
    }

    /**
     * 代わりに getItem(ItemStore.ITEM_NAME_XPHONE) を使ってください
     */
    @Deprecated(forRemoval = true)
    public ItemStack getXPhone() {
        return customItems.get(ITEM_NAME_XPHONE);
    }

    public ItemStack getItem(String key) {
        return customItems.get(key).clone();
    }

    /**
     * カスタムアイテムを作成
     */
    public ItemStack createCustomItem(String name, String... lore) {
        var st = new ItemStack(Material.KNOWLEDGE_BOOK);

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

        var meta1 = stack1.getItemMeta();
        var meta2 = stack2.getItemMeta();

        // -- 名前の比較
        var name1 = PlainTextComponentSerializer.plainText().serialize(meta1.displayName());
        var name2 = PlainTextComponentSerializer.plainText().serialize(meta2.displayName());
        if (!name1.equals(name2)) return false;
        
        // -- lore の比較
        var lore1 = meta1.lore().stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        var lore2 = meta2.lore().stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        if (!lore1.equals(lore2)) return false;

        return true;
    }

    public void givePhoneIfNeeded(@NotNull Player player) {
        var inv = player.getInventory();
        var phone = getItem(ItemStore.ITEM_NAME_XPHONE);
        var hasItem = Streams.stream(inv)
            .anyMatch(a -> compareCustomItem(a, getItem(ItemStore.ITEM_NAME_XPHONE)));
        if (!hasItem) inv.addItem(phone);
    }

    public ItemStack getPlayerHead(Player player) {
        var stack = new ItemStack(Material.PLAYER_HEAD);

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
    }
    
    private final Map<String, ItemStack> customItems = new HashMap<>();
    private static ItemStore instance;
}
