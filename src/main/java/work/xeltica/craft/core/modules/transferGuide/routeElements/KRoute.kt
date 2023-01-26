package work.xeltica.craft.core.modules.transferGuide.routeElements

import org.bukkit.Bukkit
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
                    if (path.to == stations[i + 1].id) {
                        candidates.add(
                            KRoutePathReal(
                                path.line ?: "",
                                path.direction ?: "",
                                path.time ?: -1
                            )
                        )
                    }
                }
                pathsCandidates.add(candidates)
            }
        }
        if (data.consoleDebug) {
            Bukkit.getLogger().info(pathsCandidates.joinToString { candidate ->
                "pathCandidates=[" +
                        candidate.joinToString {
                            if (it is KRoutePathReal) {
                                "${it.line}:${it.direction}"
                            } else {
                                "END"
                            }
                        } + "]"
            })
        }
        //使用可能な移動経路群から使用する経路を選択
        for (i in pathsCandidates.indices) {
            val before = if (i == 0) {
                null
            } else {
                pathsCandidates[i - 1]
            }
            val now = pathsCandidates[i]
            val after = if (i == pathsCandidates.lastIndex) {
                null
            } else {
                pathsCandidates[i + 1]
            }
            val beforeDecided = before != null && before.size == 1
            val afterDecided = after != null && after.size == 1
            if (beforeDecided && before != null) {
                val beforeD = before[0]
                if (beforeD !is KRoutePathReal) continue
                val found = now.find {
                    if (it is KRoutePathReal) {
                        it.line == beforeD.line && it.direction == beforeD.direction
                    } else {
                        false
                    }
                }
                if (found != null) {
                    pathsCandidates[i] = arrayListOf(found)
                }
            }
            if (afterDecided && after != null) {
                val afterD = after[0]
                if (afterD !is KRoutePathReal) continue
                val found = now.find {
                    if (it is KRoutePathReal) {
                        it.line == afterD.line && it.direction == afterD.direction
                    } else {
                        false
                    }
                }
                if (found != null) {
                    pathsCandidates[i] = arrayListOf(found)
                }
            }
            if (pathsCandidates[i].size >= 2) {
                pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
            }
        }
        //駅と路線を纒める
        val routesListNull = ArrayList<KRouteBlock?>()
        for (i in stations.indices) {
            routesListNull.add(KRouteBlock(stations[i], pathsCandidates[i][0]))
        }
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
            if (route != null) {
                routesList.add(route)
            }
        }
        routes = routesList.toTypedArray()
    }

    fun getTime(): Int {
        var appendTime = 0
        routes.forEachIndexed { i, it ->
            if (it.routePath is KRoutePathReal) {
                appendTime += it.routePath.time
                appendTime += if (routes.lastIndex - 1 == i) {
                    0
                } else if (it.routePath.line == "walk" || it.routePath.line == "boat") {
                    10
                } else {
                    30
                }
            }
        }
        return appendTime
    }

    fun toStringForGuide(): String {
        val sb = StringBuilder()
        var appendTime = 0
        val gray = ChatColor.GRAY
        val white = ChatColor.WHITE
        sb.append("${gray}===== ${white}結果 ${gray}=====\n")
        routes.forEachIndexed { i, it ->
            sb.append("${white}${it.station.name}${gray}(${it.station.yomi}")
            it.station.number?.run { sb.append("/${it.station.number}") }
            sb.append("/X:${it.station.location[0]},Z:${it.station.location[1]})\n")
            if (it.routePath is KRoutePathReal) {
                appendTime += it.routePath.time
                appendTime += if (routes.lastIndex - 1 == i) {
                    0
                } else if (it.routePath.line == "walk" || it.routePath.line == "boat") {
                    10
                } else {
                    30
                }
                sb.append("$white | ")
                sb.append("${it.routePath.toStringForGuide(data)}\n")
            }
        }
        sb.append("${gray}所要時間:${white}約${TransferGuideUtil.secondsToString(appendTime)}\n")
        sb.append("${gray}=".repeat(15))
        return sb.toString()
    }
}