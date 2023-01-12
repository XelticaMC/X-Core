package work.xeltica.craft.core.modules.transferGuide.dataElements

import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil

/**
 * 条件に合う駅を絞り込むコードを分かりやすくする為だけのクラスです。
 * @author Knit prg.
 */
class KStations private constructor(data: TransferGuideData) {
    var value: ArrayList<KStation>

    init {
        val v = arrayListOf<KStation>()
        data.stations.forEach {
            v.add(it.value)
        }
        value = v
    }

    companion object {
        /**
         * 路線データ内の全ての駅を取得します。
         */
        fun allStations(data: TransferGuideData): KStations {
            return KStations(data)
        }
    }

    /**
     * 先頭から[n]個の駅を抽出します。
     */
    fun fromBegin(n: Int): KStations {
        val newValue = arrayListOf<KStation>()
        for (i in 0 until n) {
            try {
                newValue.add(value[i])
            } catch (_: IndexOutOfBoundsException) {

            }
        }
        value = newValue
        return this
    }

    /**
     * 路線で駅を抽出します。
     */
    fun filterByLine(line: KLine): KStations {
        val newValue = arrayListOf<KStation>()
        line.stations.forEach { station ->
            value.find { it.id == station }?.run {
                newValue.add(this)
            }
        }
        value = newValue
        return this
    }

    /**
     * 最寄り自治体で駅を抽出します。
     */
    fun filterByMuni(muni: KMuni): KStations {
        val newValue = arrayListOf<KStation>()
        muni.stations.forEach { station ->
            value.find { it.id == station }?.run {
                newValue.add(this)
            }
        }
        value = newValue
        return this
    }

    /**
     * 特定の種類の駅のみを抽出します。
     */
    fun filterByType(type: String): KStations {
        val newValue = arrayListOf<KStation>()
        value.forEach {
            if (it.type == type) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }

    /**
     * 特定のワールドにある駅のみを抽出します。
     */
    fun filterByWorld(worldName: String): KStations {
        val newValue = arrayListOf<KStation>()
        value.forEach {
            if (it.world == worldName) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }

    /**
     * 読みが特定の文字から始まる駅のみを抽出します。
     */
    fun filterByYomiInitial(yomiInitial: String): KStations {
        val newValue = arrayListOf<KStation>()
        value.forEach {
            if (it.yomi?.first()?.toString() == yomiInitial) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }

    /**
     * 距離で並び替えます。
     */
    fun sortByDistance(coordinate: DoubleArray): KStations {
        val distanceList = ArrayList<Pair<Double, KStation>>()
        value.forEach {
            val distance = TransferGuideUtil.calcDistance(coordinate, it.location)
            distanceList.add(Pair(distance, it))
        }
        distanceList.sortBy { it.first }
        val newValue = arrayListOf<KStation>()
        distanceList.forEach {
            newValue.add(it.second)
        }
        value = newValue
        return this
    }

    /**
     * 読みで並び替えます。
     */
    fun sortByYomi(): KStations {
        value.sortBy { it.yomi }
        return this
    }
}