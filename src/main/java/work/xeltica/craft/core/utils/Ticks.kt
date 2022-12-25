package work.xeltica.craft.core.utils

/**
 * 時間をMinecraft Tickに変換するのと、その逆をする機能を持つ
 * @author Xeltica
 */
object Ticks {
    /** 秒をTickに変換  */
    @JvmStatic
    fun from(second: Double): Int {
        return (second * 20).toInt()
    }

    /** 分、秒をTickに変換  */
    @JvmStatic
    fun from(minute: Int, second: Double): Int {
        return from((minute * 60).toDouble()) + from(second)
    }

    /** 時間、分、秒をTickに変換  */
    @JvmStatic
    fun from(hour: Int, minute: Int, second: Double): Int {
        return from((hour * 3600).toDouble()) + from((minute * 60).toDouble()) + from(second)
    }

    /** Tickを秒に変換  */
    @JvmStatic
    fun toTime(tick: Int): Double {
        return tick / 20.0
    }
}