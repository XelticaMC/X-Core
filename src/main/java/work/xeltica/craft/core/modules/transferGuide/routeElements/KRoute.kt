package work.xeltica.craft.core.modules.transferGuide.routeElements

import org.bukkit.ChatColor
import work.xeltica.craft.core.modules.transferGuide.TransferGuideUtil
import work.xeltica.craft.core.modules.transferGuide.dataElements.KStation
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData

/**
 * 経路データを表すクラス
 * @author Knit prg.
 */

class KRoute(val data: TransferGuideData, stations: Array<KStation>) {
    private val routes: Array<KRouteBlock>

    init {
        val pathsCandidates = ArrayList<ArrayList<KRoutePath>>()
        //駅の配列から使用可能な移動経路群を抽出
        for (i in stations.indices) {
            if (i == stations.lastIndex) {
                pathsCandidates.add(arrayListOf(KRoutePathEnd()))
            } else {
                val candidates = ArrayList<KRoutePath>()
                for (path in stations[i].paths) {
                    if (path.to == stations[i + 1].id) candidates.add(
                        KRoutePathReal(
                            path.line,
                            path.direction,
                            path.time
                        )
                    )
                }
                pathsCandidates.add(candidates)
            }
        }
        //使用可能な移動経路群から使用する経路を選択
        for (i in pathsCandidates.indices) {
            if (pathsCandidates.size <= 1) break
            if (pathsCandidates[i].size <= 1) continue
            fun isBeforeDecided(): Boolean {
                val beforeDecided = pathsCandidates[i - 1].size == 1
                if (beforeDecided) { //前の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val before = pathsCandidates[i - 1][0]
                        if (now is KRoutePathReal && before is KRoutePathReal && now.line == before.line && now.direction == before.direction) { //前の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                }
                //見つからなければ適当に
                if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                return beforeDecided
            }

            fun isNextDecided(): Boolean {
                val nextDecided = pathsCandidates[i + 1].size == 1
                if (nextDecided) { //次の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val next = pathsCandidates[i + 1][0]
                        if (now is KRoutePathReal && next is KRoutePathReal && now.line == next.line && now.direction == next.direction) { //次の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                }
                //見つからなければ適当に
                if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                return nextDecided
            }
            if (i == 0) { //先頭の場合
                if (!isNextDecided()) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0]) //次の路線が確定していなければ適当に
            } else if (i == pathsCandidates.lastIndex) { //終端の場合
                if (!isBeforeDecided()) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0]) //前の路線が確定していなければ適当に
            } else { //中間部の場合
                if (!(isNextDecided() && isBeforeDecided())) pathsCandidates[i] =
                    arrayListOf(pathsCandidates[i][0]) //前後の路線が確定していない場合は適当に
            }
        }
        //駅と路線を纒める
        val routesListNull = ArrayList<KRouteBlock?>()
        for (i in stations.indices) routesListNull.add(KRouteBlock(stations[i], pathsCandidates[i][0]))
        //同じ路線を纒める
        for (i in routesListNull.indices) {
            if (i == 0) continue
            var j = 0
            for (k in (0 until i).reversed()) {
                if (routesListNull[k] != null) {
                    j = k
                    break
                }
            }
            val now = routesListNull[i]?.routePath
            val before = routesListNull[j]?.routePath
            if (now != null && before != null && now is KRoutePathReal && before is KRoutePathReal && now.line == before.line && now.direction == before.direction) {
                before.time += now.time
                routesListNull[i] = null
            }
        }
        val routesList = ArrayList<KRouteBlock>()
        for (route in routesListNull) {
            if (route != null) routesList.add(route)
        }
        routes = routesList.toTypedArray()
    }

    fun toStringForGuide(): String {
        val sb = StringBuilder()
        var appendTime = 0
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        sb.append("${gray}===== ${white}結果 ${gray}=====\n")
        routes.forEach {
            sb.append("${white}${it.station.name}${gray}(${it.station.yomi}")
            it.station.number?.run { sb.append("/${it.station.number}") }
            sb.append("/X:${it.station.location[0]},Z:${it.station.location[1]})\n")
            if (it.routePath is KRoutePathReal) {
                appendTime += it.routePath.time
                appendTime += if (it.routePath.line == "walk") {
                    10
                } else {
                    30
                }
                sb.append("${white} | ")
                sb.append("${it.routePath.toStringForGuide(data)}\n")
            }
        }
        sb.append("${gray}所要時間:${white}約${TransferGuideUtil.secondsToString(appendTime)}\n")
        sb.append("${white}=".repeat(20))
        return sb.toString()
    }
}