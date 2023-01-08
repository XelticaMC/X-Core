package work.xeltica.craft.core.modules.transferGuide.routeElements

import work.xeltica.craft.core.modules.transferGuide.dataElements.KStation

/**
 * 経路データ内の駅とその次の乗り換えまでの移動を表すクラス
 * @author Knit prg.
 */

class KRouteBlock(
    val station: KStation,
    val routePath: KRoutePath,
)