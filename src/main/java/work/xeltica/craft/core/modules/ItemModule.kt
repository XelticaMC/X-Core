package work.xeltica.craft.core.modules

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * カスタムアイテムを管理します。
 * @author Xeltica
 */
object ItemModule : ModuleBase() {
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
        return (customItems[key] ?: return null).clone()
    }

    /**
     * カスタムアイテムを作成
     */
    @JvmStatic
    fun createCustomItem(name: String, vararg lore: String): ItemStack {
        val st = ItemStack(Material.KNOWLEDGE_BOOK)

        st.editMeta {
            it.displayName(
                Component.text(name).style(Style.style(TextColor.color(37, 113, 255), TextDecoration.BOLD))
            )
            it.lore(lore.map { l -> Component.text(l).asComponent() })
        }
        return st
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

        // -- 名前の比較
        val name1 = PlainTextComponentSerializer.plainText().serialize(meta1.displayName() ?: return false)
        val name2 = PlainTextComponentSerializer.plainText().serialize(meta2.displayName() ?: return false)
        if (name1 != name2) return false

        // -- lore の比較
        val lore1 = (meta1.lore() ?: return false).map { PlainTextComponentSerializer.plainText().serialize(it) }.toList()
        val lore2 = (meta1.lore() ?: return false).map { PlainTextComponentSerializer.plainText().serialize(it) }.toList()
        return lore1 == lore2
    }

    @JvmStatic
    fun givePhoneIfNeeded(player: Player) {
        val inv = player.inventory
        val phone = getItem(ITEM_NAME_XPHONE) ?: throw IllegalStateException()
        val hasItem = inv.any { compareCustomItem(it, getItem(ITEM_NAME_XPHONE)) }
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

    private fun resolvePlayerHeadWithBukkit(player: Player, meta: SkullMeta) {
        meta.playerProfile = player.playerProfile
    }

    private fun registerItems() {
        customItems[ITEM_NAME_XPHONE] = createCustomItem("X Phone SE", "XelticaMCの独自機能にアクセスできるスマホ。")
        customItems[ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT] = createCustomItem("海底神殿行き資源ワールド旅行券", "メイン ✈ 海底神殿")
    }

    private val customItems: MutableMap<String, ItemStack> = mutableMapOf()
}
