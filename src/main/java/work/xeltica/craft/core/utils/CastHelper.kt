package work.xeltica.craft.core.utils

class CastHelper {
    companion object {
        inline fun <reified T> checkList(list: List<*>): List<T> {
            val copy = mutableListOf<T>()
            for (e in list) {
                copy.add(e as T)
            }
            return copy
        }

        inline fun <reified T1, reified T2> checkMap(map: Map<*, *>): Map<T1, T2> {
            val copy = mutableMapOf<T1, T2>()
            for ((key, element) in map) {
                copy[key as T1] = element as T2
            }
            return copy
        }
    }
}