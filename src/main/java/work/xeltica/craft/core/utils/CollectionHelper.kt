package work.xeltica.craft.core.utils

/**
 * コレクションクラスに対する拡張メソッドを定義します。
 */
object CollectionHelper {
    /**
     * グループにおいて、合計値を算出してマップ化します。
     */
    inline fun <T, K> Grouping<T, K>.sum(valueSelector: (T) -> Int): Map<K, Int> {
        return this.fold(0) { acc, el -> acc + valueSelector(el) }
    }
}