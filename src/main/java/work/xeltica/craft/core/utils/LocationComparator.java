package work.xeltica.craft.core.utils;

import org.bukkit.Location;

/**
 * 2つのLocationが一致しているかどうかを計算します。
 * @author Xeltica
 */
public class LocationComparator {
    public static boolean equals(Location l1, Location l2) {
        if (!l1.getWorld().equals(l2.getWorld())) return false;

        final var x1 = l1.getX();
        final var y1 = l1.getY();
        final var z1 = l1.getZ();

        final var x2 = l2.getX();
        final var y2 = l2.getY();
        final var z2 = l2.getZ();
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
