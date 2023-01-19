package work.xeltica.craft.core.utils

/**
 * 秒単位の時間とMinecraft Tickの相互変換を提供します。
 * @author Lutica
 */
object Ticks {
    /** 秒をTickに変換  */
    fun from(second: Double): Int {
        return (second * 20).toInt()
    }

    /** 分、秒をTickに変換  */
    fun from(minute: Int, second: Double): Int {
        return from((minute * 60).toDouble()) + from(second)
    }

    /** 時間、分、秒をTickに変換  */
    fun from(hour: Int, minute: Int, second: Double): Int {
        return from((hour * 3600).toDouble()) + from((minute * 60).toDouble()) + from(second)
    }

    /** Tickを秒に変換  */
    fun toTime(tick: Int): Double {
        return tick / 20.0
    }
}