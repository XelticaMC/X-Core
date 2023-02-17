package work.xeltica.craft.core.modules.xMusicDisc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.item.ItemModule

object XMusicDiscModule : ModuleBase() {
    private val X_RECORD = "X レコード"

    const val ITEM_NAME_X_MUSIC_DISC = "x_music_disc"

    override fun onEnable() {
        registerHandler(XMusicDiscHandler())
    }

    override fun onPostEnable() {
        registerDisc("csikospost", "Csikós Post")
        registerDisc("submerged2", "Submerged")
        registerDisc("submerged3", "Submerged 2")
        registerDisc("natsumatsuri", "みんなの夏物語")
        registerDisc("fb", "花火師達の弾幕")
    }

    fun registerDisc(songId: String, displayName: String) {
        ItemModule.registerItem("$ITEM_NAME_X_MUSIC_DISC.$songId", createDisc(songId, displayName))
    }

    fun createDisc(songId: String, displayName: String): ItemStack {
        val stack = ItemStack(Material.MUSIC_DISC_CAT, 1)
        stack.editMeta {
            it.displayName(Component.text("$X_RECORD - $displayName"))
            it.lore(
                listOf(
                    Component.text(songId)
                )
            )
        }
        return stack
    }

    fun ItemStack.getXMusicDiscSongId(): String? {
        if (this.type != Material.MUSIC_DISC_CAT) return null
        val songName = this.lore()?.firstOrNull() as? TextComponent ?: return null

        return songName.content()
    }
}