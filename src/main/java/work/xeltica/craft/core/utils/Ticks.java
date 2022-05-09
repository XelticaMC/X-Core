package work.xeltica.craft.core.utils;

/**
 * 時間をMinecraft Tickに変換するのと、その逆をする機能を持つ
 * @author Xeltica
 */
public class Ticks {
    /** 秒をTickに変換 */
    public static int from(double second) {
        return (int)(second * 20);
    }

    /** 分、秒をTickに変換 */
    public static int from(int minute, double second) {
        return from(minute * 60) + from(second);
    }

    /** 時間、分、秒をTickに変換 */
    public static int from(int hour, int minute, double second) {
        return from(hour * 3600) + from(minute * 60) + from(second);
    }

    /** Tickを秒に変換 */
    public static double toTime(int tick) {
        return tick / 20.0;
    }
}
