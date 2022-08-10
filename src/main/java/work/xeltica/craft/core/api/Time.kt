package work.xeltica.craft.core.api

object Time {
    @JvmStatic
    fun msToString(timeInMilliseconds: Long): String {
        val ms = timeInMilliseconds % 1000
        val _s = timeInMilliseconds / 1000
        val s = _s % 60
        val _m = _s / 60
        val m = _m % 60
        val _h = _m / 60
        val h = _h % 60
        return if (h == 0L) String.format(
            "%02d:%02d.%03d",
            m,
            s,
            ms
        ) else String.format("%02d:%02d:%02d.%03d", h, m, s, ms)
    }
}