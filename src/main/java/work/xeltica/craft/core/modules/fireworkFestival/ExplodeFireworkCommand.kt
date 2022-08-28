package work.xeltica.craft.core.modules.fireworkFestival

data class ExplodeFireworkCommand (
    val loc: Array<Double> = arrayOf(0.0, 0.0, 0.0),
    val random: Int = 0,
    val clone: Int = 0,
    val cloneTick: Int = 0,
) : FireworkCommandBase() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExplodeFireworkCommand

        if (!loc.contentEquals(other.loc)) return false
        if (random != other.random) return false
        if (clone != other.clone) return false
        if (cloneTick != other.cloneTick) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loc.contentHashCode()
        result = 31 * result + random
        result = 31 * result + clone
        result = 31 * result + cloneTick
        return result
    }
}