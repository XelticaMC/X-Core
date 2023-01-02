package work.xeltica.craft.core.modules.eventSummer

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import work.xeltica.craft.core.api.ModuleBase
import java.time.LocalDate
import java.util.Random
import java.util.UUID

object EventSummerModule : ModuleBase() {
    const val PS_KEY_LOGIN_BONUS_SUMMER = "login_bonus_summer"

    private val colors = listOf(
        Color.fromRGB(0xe23731), // red
        Color.fromRGB(0xeb6101), // vermilion
        Color.fromRGB(0xf08300), // orange
        Color.fromRGB(0xe9be00), // yellow
        Color.fromRGB(0xb8d200), // lime
        Color.fromRGB(0x3eb370), // green
        Color.fromRGB(0x20c0a0), // teal
        Color.fromRGB(0x43fcf3), // cyan
        Color.fromRGB(0x00b7ff), // skyblue
        Color.fromRGB(0x2571ff), // blue
        Color.fromRGB(0xff55a1), // magenta
        Color.fromRGB(0xff5c84), // pink
    )

    override fun onEnable() {
        registerHandler(EventSummerHandler())
    }

    @JvmStatic
    fun isEventNow(): Boolean {
        val today = LocalDate.now()
        val startEventDay = LocalDate.of(2022, 8, 15)
        val endEventDay = LocalDate.of(2022, 9, 1)
        return today.isAfter(startEventDay) && today.isBefore(endEventDay)
    }

    fun getRandomFireworkByUUID(uuid: UUID, amount: Int): ItemStack {
        val random = Random(uuid.hashCode().toLong())
        val item = ItemStack(Material.FIREWORK_ROCKET, amount)
        item.editMeta {
            if (it !is FireworkMeta) return@editMeta
            val effect = FireworkEffect.builder()
                .trail(random.nextBoolean())
                .flicker(random.nextBoolean())
                .with(FireworkEffect.Type.values()[random.nextInt(5)])
                .withColor(colors[random.nextInt(colors.size)])
                .build()
            it.addEffect(effect)
            it.power = 1
        }
        return item
    }
}