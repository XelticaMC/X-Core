package work.xeltica.craft.core.modules.eventFirework

import org.bukkit.FireworkEffect

data class FireOperation(
    val type: FireworkEffect.Type,
    val colors: List<String> = listOf("RANDOM"),
    val fades: List<String> = listOf(),
    val flicker: Boolean = false,
    val trail: Boolean = false,
    val loc: List<Double> = listOf(0.0, 0.0, 0.0),
    val random: Int = 0,
    val clone: Int = 0,
    val power: Int = 1,
) : OperationBase()