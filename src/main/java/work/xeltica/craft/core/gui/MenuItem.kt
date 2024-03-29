package work.xeltica.craft.core.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * メニューのアイテム。
 *
 * @author Lutica
 */
class MenuItem(
    val name: String,
    val onClick: Consumer<MenuItem>?,
    val icon: ItemStack,
    val customData: Any? = null,
    val isShiny: Boolean = false,
) {

    constructor(name: String, onClick: Consumer<MenuItem>?, icon: Material, customData: Any?, shiny: Boolean)
            : this(name, onClick, icon, customData, 1, shiny)

    constructor(name: String, onClick: Consumer<MenuItem>? = null, icon: Material = Material.STONE_BUTTON, customData: Any? = null, count: Int = 1, shiny: Boolean = false)
            : this(name, onClick, ItemStack(icon, count), customData, shiny)
}
