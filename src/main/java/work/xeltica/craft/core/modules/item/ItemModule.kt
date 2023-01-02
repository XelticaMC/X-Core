package work.xeltica.craft.core.modules.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import work.xeltica.craft.core.api.ModuleBase

object ItemModule : ModuleBase() {
    const val ITEM_NAME_XPHONE = "xphone"
    const val ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT = "ticket_wildareab_ocean_monument"

    private val customItems: HashMap<String, ItemStack> = HashMap()

    override fun onEnable() {
        registerItems()
        Bukkit.getOnlinePlayers().forEach(this::givePhoneIfNeeded)

        registerCommand("givecustomitem", GiveCustomItemCommand())
        registerHandler(ItemHandler())
    }

    fun getItem(key: String): ItemStack {
        if (!customItems.contains(key)) throw IllegalArgumentException()
        return customItems[key]!!.clone()
    }

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

    fun compareCustomItem(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.type != stack2.type) return false

        val meta1 = stack1.itemMeta
        val meta2 = stack2.itemMeta

        val name1 = PlainTextComponentSerializer.plainText().serialize(meta1.displayName()!!)
        val name2 = PlainTextComponentSerializer.plainText().serialize(meta2.displayName()!!)
        if (name1 != name2) return false

        val lore1 = meta1.lore()?.map { PlainTextComponentSerializer.plainText().serialize(it) }
        val lore2 = meta2.lore()?.map { PlainTextComponentSerializer.plainText().serialize(it) }
        return lore1 == lore2
    }

    fun givePhoneIfNeeded(player: Player) {
        val inv = player.inventory
        val phone = getItem(ITEM_NAME_XPHONE)
        val hasItem = inv.any { compareCustomItem(it, phone) }
        if (!hasItem) inv.addItem(phone)
    }

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
        customItems[ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT] =
            createCustomItem("海底神殿行き資源ワールド旅行券", "メイン ✈ 海底神殿")
    }
}