package work.xeltica.craft.core.modules

import com.destroystokyo.paper.profile.ProfileProperty
import java.util.Objects
import java.util.stream.Stream

import com.google.common.collect.Streams

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.annotations.NotNull

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import java.util.Arrays

/**
 * カスタムアイテムを管理します。
 * TODO: 近いうちにカスタムアイテムの管理方法を大きく変更する予定です…
 * @author Xeltica
 */
object CustomItemModule : ModuleBase() {
    @JvmStatic
    val ITEM_NAME_XPHONE = "xphone"
    @JvmStatic
    val ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT = "ticket_wildareab_ocean_monument"

    override fun onEnable() {
        registerItems()
        Bukkit.getOnlinePlayers().forEach(this::givePhoneIfNeeded)
    }

    @JvmStatic
    fun getItem(key: String): ItemStack? {
        return customItems[key]?.clone()
    }

    /**
     * カスタムアイテムを作成
     */
    @JvmStatic
    fun createCustomItem(name: String, vararg lore: String): ItemStack {
        val stack = ItemStack(Material.KNOWLEDGE_BOOK)

        stack.editMeta {
            it.displayName(
                Component.text(name).style(Style.style(TextColor.color(37, 113, 255), TextDecoration.BOLD))
            )
            it.lore(lore.map { l -> Component.text(l).asComponent() })
        }
        return stack
    }

    /**
     * カスタムアイテムの比較
     */
    @JvmStatic
    fun compareCustomItem(stack1: ItemStack?, stack2: ItemStack?): Boolean {
        // -- どっちもnull
        if (stack1 == null && stack2 == null) return true

        // -- どっちかがnull
        if (stack1 == null || stack2 == null) return false

        // -- 種類が違う
        if (stack1.type != stack2.type) return false

        val meta1 = stack1.itemMeta
        val meta2 = stack2.itemMeta

        // -- 名前の比較。表示名がないということは、カスタムアイテムではない なのでreturn false
        val name1 = PlainTextComponentSerializer.plainText().serialize(meta1.displayName() ?: return false)
        val name2 = PlainTextComponentSerializer.plainText().serialize(meta2.displayName() ?: return false)
        if (!name1.equals(name2)) return false

        // -- lore の比較
        val lore1 = (meta1.lore() ?: return false).map { PlainTextComponentSerializer.plainText().serialize(it) }.toTypedArray()
        val lore2 = (meta2.lore() ?: return false).map { PlainTextComponentSerializer.plainText().serialize(it) }.toTypedArray()
        return lore1 contentEquals lore2
    }

    @JvmStatic
    fun givePhoneIfNeeded(player: Player) {
        val inv = player.inventory
        val phone = getItem(ITEM_NAME_XPHONE) ?: throw IllegalStateException("X Phoneが登録されていない")
        val hasItem = inv.any {
            compareCustomItem(it, getItem(ITEM_NAME_XPHONE))
        }
        if (!hasItem) inv.addItem(phone)
    }

    @JvmStatic
    fun getPlayerHead(player: Player): ItemStack {
        val stack = ItemStack(Material.PLAYER_HEAD)

        stack.editMeta {
            if (it is SkullMeta) {
                resolvePlayerHeadWithBukkit(player, it)
            }
        }
        return stack
    }

//    private fun resolvePlayerHeadWithSkinsRestorer(player: Player, meta: SkullMeta) {
//        val sapi = SkinsRestorerAPI.getApi()
//
//        val skinName = sapi.getSkinName(player.name)
//        if (skinName == null) {
//            resolvePlayerHeadWithBukkit(player, meta)
//            return
//        }
//        val skin = sapi.getSkinData(skinName)
//
//        val profile = Bukkit.createProfile(skin.getName())
//        profile.setProperty(ProfileProperty("textures", skin.getValue(), skin.getSignature()))
//        meta.playerProfile = profile
//    }

    private fun resolvePlayerHeadWithBukkit(player: Player, meta: SkullMeta) {
        meta.playerProfile = player.playerProfile
    }

    private fun registerItems() {
        customItems.put(ITEM_NAME_XPHONE, createCustomItem("X Phone SE", "XelticaMCの独自機能にアクセスできるスマホ。"))
        customItems.put(ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT, createCustomItem("海底神殿行きワイルドエリアB旅行券", "メイン ✈ 海底神殿"))
    }

    private val customItems = mutableMapOf<String, ItemStack>()
}
