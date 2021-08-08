package work.xeltica.craft.core.utils;

public class Time {
    public static String msToString(long timeInMilliseconds) {
        final var _ms = timeInMilliseconds;
        final var ms = _ms % 1000;
        final var _s = _ms / 1000;
        final var s = _s % 60;
        final var _m = _s / 60;
        final var m = _m % 60;
        final var _h = _m / 60;
        final var h = _h % 60;
        return h == 0
            ? String.format("%02d:%02d.%03d", m, s, ms)
            : String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
    }
}
