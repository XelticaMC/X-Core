package work.xeltica.craft.core.modules.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.xphone.XphoneModule

/**
 * カスタムアイテムを管理するモジュールです。
 */
object ItemModule : ModuleBase() {
    @Deprecated(
        "XphoneModule に移行しました。", replaceWith = ReplaceWith(
            "XphoneModule.ITEM_NAME_XPHONE",
            "work.xeltica.craft.core.modules.xphone.XphoneModule"
        )
    )
    const val ITEM_NAME_XPHONE = "xphone"
    const val ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT = "ticket_wildareab_ocean_monument"

    private val customItems: HashMap<String, ItemStack> = HashMap()

    override fun onEnable() {
        registerItems()

        registerCommand("givecustomitem", GiveCustomItemCommand())
        registerHandler(ItemHandler())
        registerHandler(TicketWildareaBHandler())
    }

    /**
     * [key] に対応するカスタムアイテムを取得します。
     */
    fun getItem(key: String): ItemStack {
        val item = customItems[key] ?: throw IllegalArgumentException()
        return item.clone()
    }

    /**
     * このアイテムがカスタムアイテムであるかどうかを取得します。
     */
    fun ItemStack.isCustomItem(): Boolean {
        return this.hasLore()
    }

    /**
     * このアイテムが lore プロパティを保有しているかどうかを取得します。
     */
    fun ItemStack.hasLore(): Boolean {
        return this.lore()?.isNotEmpty() ?: false
    }

    /**
     * カスタムアイテムを作成します。
     */
    fun createCustomItem(name: String, vararg lore: String): ItemStack {
        val st = ItemStack(Material.KNOWLEDGE_BOOK)

        st.editMeta {
            it.displayName(
                Component.text(name).style(Style.style(TextColor.color(37, 113, 255), TextDecoration.BOLD))
            )
            it.lore(lore.map { s -> Component.text(s) })
        }
        return st
    }

    /**
     * [stack1] と [stack2] が等しいかどうかを検証します。
     */
    fun compareCustomItem(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.type != stack2.type) return false

        val meta1 = stack1.itemMeta
        val meta2 = stack2.itemMeta

        val name1 = PlainTextComponentSerializer.plainText().serialize(meta1.displayName() ?: return false)
        val name2 = PlainTextComponentSerializer.plainText().serialize(meta2.displayName() ?: return false)
        if (name1 != name2) return false

        val lore1 = meta1.lore()?.map { PlainTextComponentSerializer.plainText().serialize(it) }
        val lore2 = meta2.lore()?.map { PlainTextComponentSerializer.plainText().serialize(it) }
        return lore1 == lore2
    }

    /**
     * 必要に応じて [player] にX Phoneを渡します。
     */
    fun givePhoneIfNeeded(player: Player) {
        val inv = player.inventory
        val phone = getItem(XphoneModule.ITEM_NAME_XPHONE)
        val hasItem = inv.any { compareCustomItem(it, phone) }
        if (!hasItem) inv.addItem(phone)
    }

    /**
     * 指定したプレイヤーの頭を取得します。
     */
    fun getPlayerHead(player: Player): ItemStack {
        val stack = ItemStack(Material.PLAYER_HEAD)
        stack.editMeta {
            if (it is SkullMeta) {
                resolvePlayerHeadWithBukkit(player, it)
            }
        }
        return stack
    }

    /**
     * ItemModule にアイテムを登録します。
     */
    fun registerItem(key: String, item: ItemStack) {
        customItems[key] = item.clone()
    }

    /**
     * 登録されているアイテム名の一覧を取得します。
     */
    fun getCustomItemNames(): Set<String> {
        return customItems.keys
    }

    /**
     * Bukkit APIを用いてプレイヤーの頭情報を解決します。
     */
    private fun resolvePlayerHeadWithBukkit(player: Player, meta: SkullMeta) {
        meta.playerProfile = player.playerProfile
    }

    /**
     * カスタムアイテムを定義します。
     */
    private fun registerItems() {
        customItems[ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT] =
            createCustomItem("海底神殿行き資源ワールド旅行券", "メイン ✈ 海底神殿")
    }
}