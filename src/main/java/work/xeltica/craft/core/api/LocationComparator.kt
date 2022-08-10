package work.xeltica.craft.core.api

import org.bukkit.Location

/**
 * 2つのLocationが一致しているかどうかを計算します。
 * @author Xeltica
 */
object LocationComparator {
    fun equals(l1: Location, l2: Location): Boolean {
        if (l1.world != l2.world) return false
        val x1 = l1.x
        val y1 = l1.y
        val z1 = l1.z
        val x2 = l2.x
        val y2 = l2.y
        val z2 = l2.z
        return x1 == x2 && y1 == y2 && z1 == z2
    }
}