package work.xeltica.craft.core.modules.transferGuide.dataElements

import work.xeltica.craft.core.modules.transferGuide.TransferGuideModule

/**
 * 自治体絞り込み用
 * @author Knit prg.
 */
class KMunis private constructor() {
    var value: ArrayList<KMuni>

    init {
        val v = arrayListOf<KMuni>()
        TransferGuideModule.data.municipalities.forEach {
            v.add(it.value)
        }
        value = v
    }

    companion object {
        /**
         * 路線データ内の全ての自治体を取得します。
         */
        fun allMunis(): KMunis {
            return KMunis()
        }
    }

    /**
     * ワールドで自治体を抽出します。
     */
    fun filterByWorld(worldName: String): KMunis {
        val newValue = arrayListOf<KMuni>()
        value.forEach {
            if (it.world == worldName) {
                newValue.add(it)
            }
        }
        value = newValue
        return this
    }
}