package work.xeltica.craft.core.modules.transferGuide.dataElements

import work.xeltica.craft.core.modules.transferGuide.TransferGuideModule

/**
 * 路線絞り込み用
 * @author Knit prg.
 */
class KLines private constructor() {
    var value: ArrayList<KLine>

    init {
        val v = arrayListOf<KLine>()
        TransferGuideModule.data.lines.forEach {
            v.add(it.value)
        }
        value = v
    }

    companion object {
        /**
         * 路線データ内の全ての路線を取得します。
         */
        fun allLines(): KLines {
            return KLines()
        }
    }

    /**
     * 会社で路線を抽出します。
     */
    fun filterByCompany(company: KCompany): KLines {
        val newValue = arrayListOf<KLine>()
        value.forEach {
            if (company.lines.contains(it.id)) {
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
        val newValue = arrayListOf<KLine>()
        value.forEach {
            if (it.world == worldName) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }
}