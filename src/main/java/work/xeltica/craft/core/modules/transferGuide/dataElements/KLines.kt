package work.xeltica.craft.core.modules.transferGuide.dataElements

/**
 * 路線絞り込み用
 * @author Knit prg.
 */
class KLines private constructor(data: TransferGuideData) {
    var value: ArrayList<Pair<String, KLine>>

    init {
        val v = arrayListOf<Pair<String, KLine>>()
        data.lines.forEach {
            v.add(Pair(it.key, it.value))
        }
        value = v
    }

    companion object {
        /**
         * 路線データ内の全ての路線を取得します。
         */
        fun allLines(data: TransferGuideData): KLines {
            return KLines(data)
        }
    }

    /**
     * 会社で路線を抽出します。
     */
    fun filterByCompany(company: KCompany): KLines {
        val newValue = arrayListOf<Pair<String, KLine>>()
        value.forEach {
            if (company.lines.contains(it.first)) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }

    /**
     * 特定のワールドにある路線のみを抽出します。
     */
    fun filterByWorld(worldName: String): KLines {
        val newValue = arrayListOf<Pair<String, KLine>>()
        value.forEach {
            if (it.second.world == worldName) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }
}