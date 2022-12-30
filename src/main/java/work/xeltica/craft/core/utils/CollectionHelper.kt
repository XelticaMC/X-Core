package work.xeltica.craft.core.utils

object CollectionHelper {
    inline fun <T, K> Grouping<T, K>.sum(valueSelector: (T) -> Int): Map<K, Int> {
        return this.fold(0) { acc, el -> acc + valueSelector(el) }
    }
}