package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.utils.Config
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class TransferGuideSession(val player: Player) {
    private val data = TransferGuideData()
    private val gui = Gui.getInstance()
    private val logger = Bukkit.getLogger()
    private var startId: String? = null
    private var endId: String? = null

    fun start() {
        openMainMenu()
    }

    private fun chooseStation(startOrEnd: StartOrEnd) {
        when (player.world.name) {
            "main" -> chooseStationMain(startOrEnd)
            "wildarea2", "wildarea2_nether" -> chooseStationWild(startOrEnd)
            else -> gui.error(player, "今いるワールドには鉄道は登録されていません！")
        }
    }

    private fun chooseStationMain(startOrEnd: StartOrEnd) {
        gui.openMenu(
            player, "駅選択", listOf(
                MenuItem("近い順", { chooseStationNear(startOrEnd) }, Material.DIAMOND_HORSE_ARMOR),
                MenuItem("五十音別", { chooseStationAiueo(startOrEnd) }, Material.BOOK),
                MenuItem("会社・路線別", { chooseStationLine(startOrEnd) }, Material.SPRUCE_DOOR),
                MenuItem("近隣自治体別", { chooseStationMuni(startOrEnd) }, Material.FILLED_MAP),
            )
        )
    }

    private fun chooseStationAiueo(startOrEnd: StartOrEnd) {
        val items = ArrayList<MenuItem>()
        for (column in JapaneseColumns.values()) {
            items.add(MenuItem("${column.firstChar}行", { chooseStationAiueo(startOrEnd, column) }, Material.BOOK))
        }
        items.add(MenuItem("戻る", { chooseStation(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "五十音別", items)
    }

    private fun chooseStationAiueo(startOrEnd: StartOrEnd, column: JapaneseColumns) {
        val items = ArrayList<MenuItem>()
        for (char in column.chars) {
            items.add(MenuItem(char, { chooseStationAiueo(startOrEnd, column, char) }, Material.BOOK))
        }
        items.add(MenuItem("戻る", { chooseStationAiueo(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${column.firstChar}行", items)
    }

    private fun chooseStationAiueo(startOrEnd: StartOrEnd, column: JapaneseColumns, char: String) {
        val stations = ArrayList<KStation>()
        for (station in data.stations.values) {
            if (station.world == player.world.name && station.yomi.first().toString() == char) stations.add(station)
        }
        stations.sortBy { it.yomi }
        val items = ArrayList<MenuItem>()
        for (station in stations) {
            when (startOrEnd) {
                StartOrEnd.START -> items.add(
                    MenuItem(
                        station.name,
                        { startId = station.id;openMainMenu() },
                        Material.MINECART
                    )
                )

                StartOrEnd.END -> items.add(
                    MenuItem(
                        station.name,
                        { endId = station.id;openMainMenu() },
                        Material.MINECART
                    )
                )
            }
        }
        items.add(MenuItem("戻る", { chooseStationAiueo(startOrEnd, column) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${char}から始まる駅一覧", items)
    }

    private fun chooseStationLine(startOrEnd: StartOrEnd) {
        val items = ArrayList<MenuItem>()
        for (company in data.companies.values) {
            items.add(MenuItem(company.name, { chooseStationLine(startOrEnd, company) }, Material.SPRUCE_DOOR))
        }
        items.add(MenuItem("戻る", { chooseStation(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "会社選択", items)
    }

    private fun chooseStationLine(startOrEnd: StartOrEnd, company: KCompany) {
        val items = ArrayList<MenuItem>()
        for (line in company.lines) {
            items.add(
                MenuItem(
                    data.lines[line]?.name ?: line,
                    { chooseStationLine(startOrEnd, company, data.lines[line]) },
                    Material.RAIL
                )
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, company.name ?: "路線選択", items)
    }

    private fun chooseStationLine(startOrEnd: StartOrEnd, company: KCompany, line: KLine?) {
        if (line == null) {
            gui.error(player, "存在しない路線がデータ内に存在します。")
            logger.warning("[TransferGuide] 存在しない路線データ(${company.name}内)")
            return
        }
        val items = ArrayList<MenuItem>()
        for (stationId in line.stations) {
            val station = data.getStation(stationId)
            if (station?.world == player.world.name) {
                when (startOrEnd) {
                    StartOrEnd.START -> items.add(
                        MenuItem(
                            station.name,
                            { startId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )

                    StartOrEnd.END -> items.add(
                        MenuItem(
                            station.name,
                            { endId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )
                }
            }
        }
        items.add(MenuItem("戻る", { chooseStationLine(startOrEnd, company) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, line.name ?: "駅選択", items)
    }

    private fun chooseStationMuni(startOrEnd: StartOrEnd) {
        val items = ArrayList<MenuItem>()
        for (muni in data.municipalities) {
            items.add(MenuItem(muni.value, { chooseStationMuni(startOrEnd, muni.key) }, Material.FILLED_MAP))
        }
        items.add(MenuItem("戻る", { chooseStation(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "自治体選択", items)
    }

    private fun chooseStationMuni(startOrEnd: StartOrEnd, muniId: String) {
        val items = ArrayList<MenuItem>()
        for (station in data.stations.values) {
            if (station.world == player.world.name && station.municipality.contains(muniId)) {
                when (startOrEnd) {
                    StartOrEnd.START -> items.add(
                        MenuItem(
                            station.name,
                            { startId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )

                    StartOrEnd.END -> items.add(
                        MenuItem(
                            station.name,
                            { endId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )
                }
            }
        }
        items.add(MenuItem("戻る", { chooseStationMuni(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${data.municipalities[muniId]}", items)
    }

    private fun chooseStationNear(startOrEnd: StartOrEnd) {
        val stations: MutableMap<Double, KStation> = mutableMapOf()
        for (station in data.stations.values) {
            if (station.world == player.world.name) {
                val distance = getDistance(doubleArrayOf(player.location.x, player.location.z), station.location)
                stations[distance] = station
            }
        }
        val distances = stations.keys.sorted()
        val items = ArrayList<MenuItem>()
        for (i in 0..11) {
            try {
                val station = stations.getValue(distances[i])
                when (startOrEnd) {
                    StartOrEnd.START -> items.add(
                        MenuItem(
                            "${station.name}(約${distances[i].toInt()}m)",
                            { startId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )

                    StartOrEnd.END -> items.add(
                        MenuItem(
                            "${station.name}(約${distances[i].toInt()}m)",
                            { endId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
        items.add(MenuItem("戻る", { chooseStation(startOrEnd) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "近い順", items)
    }

    private fun chooseStationWild(startOrEnd: StartOrEnd) {
        val items = ArrayList<MenuItem>()
        for (station in data.stations.values) {
            if (station.world == player.world.name) {
                when (startOrEnd) {
                    StartOrEnd.START -> items.add(
                        MenuItem(
                            station.name,
                            { startId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )

                    StartOrEnd.END -> items.add(
                        MenuItem(
                            station.name,
                            { endId = station.id;openMainMenu() },
                            Material.MINECART
                        )
                    )
                }
            }
        }
        gui.openMenu(player, "駅一覧", items)
    }

    private fun calcRoute() {
        if (startId == null && endId == null) {
            gui.error(player, "駅が設定されていません！")
            return
        } else if (startId == endId) {
            gui.error(player, "出発地点と到着地点が同一です。")
            return
        } else if (startId == null) {
            gui.error(player, "出発地点が設定されていません！")
            return
        } else if (endId == null) {
            gui.error(player, "到着地点が設定されていません！")
            return
        }
        val start = data.getStation(startId)
        if (start == null) {
            gui.error(player, "出発地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅:${startId}")
            return
        }
        val end = data.getStation(endId)
        if (end == null) {
            gui.error(player, "到着地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅:${endId}")
            return
        }
        /*
            アルゴリズムの大まかなイメージ
            A*のつもりだけどちゃんと実装できているかは不明
            1. 全ての駅を表すカードが裏返しで置いてあると想定する。(unsearched)
            2. 出発地点の駅のカードを開ける。(unsearchedから削除、openedへ追加)
            3. 既に開いているカードの内、最も到着地点の駅への直線距離が短い駅(minStation)の隣の駅を全て開け(openedへ追加)、
               その駅に印をつける(closedに追加しopenedから削除)。
            4. 開いたカードに到着地点の駅が含まれていなければ3. へ戻る。
            5. 印をつけた駅を結んで、おわり。
        */
        val unsearched = data.stations.toMutableMap()
        val opened: MutableMap<KStation, Double> = mutableMapOf()
        val closed: MutableSet<KStation> = mutableSetOf()
        opened[start] = getDistance(start.location, end.location)
        unsearched.remove(startId)
        var i = 0
        knitA@ while (i < data.loopMax) {
            if (data.consoleDebug) {
                logger.info(
                    "[TransferGuide] step: A${i}\nunsearched: ${
                        unsearched.toList().joinToString { it.first }
                    }\nopened: ${opened.toList().joinToString { it.first.id }}\nclosed: ${
                        closed.joinToString { it.id }
                    }"
                )
            }
            val minStationEntry = opened.minByOrNull { it.value }
            if (minStationEntry == null) {
                gui.error(player, "最小値からの駅探索Aに失敗しました。")
                logger.warning(
                    "[TransferGuide] 最小値からの駅探索失敗A(${startId}->${endId}, unsearched: ${
                        unsearched.toList().joinToString { it.first }
                    }, opened: ${opened.toList().joinToString { it.first.id }}, closed: ${
                        closed.joinToString { it.id }
                    })"
                )
                return
            }
            closed.add(minStationEntry.key)
            opened.remove(minStationEntry.key)
            for (path in minStationEntry.key.paths) {
                val pathToInUnsearched = unsearched[path.to] ?: continue
                opened[pathToInUnsearched] = getDistance(end.location, pathToInUnsearched.location)
                unsearched.remove(pathToInUnsearched.id)
                if (endId == pathToInUnsearched.id) {
                    closed.add(pathToInUnsearched)
                    logger.info(
                        "[TransferGuide] step: A_END,unsearched: ${
                            unsearched.toList().joinToString { it.first }
                        }, opened: ${opened.toList().joinToString { it.first.id }}, closed: ${
                            closed.joinToString { it.id }
                        }"
                    )

                    break@knitA
                }
            }
            i++
        }
        val routeArrayList = ArrayList<KStation>()
        routeArrayList.add(end)
        closed.removeIf { it.id == endId }
        var j = 0
        knitB@ while (j < data.loopMax) {
            if (data.consoleDebug) {
                logger.info("[TransferGuide] step: B${j}\nclosed: ${closed.joinToString { it.id }}\nrouteArrayList: ${routeArrayList.joinToString { it.id }}")
            }
            val lastStationPath = routeArrayList.last().paths
            val candidates: MutableMap<KStation, Double> = mutableMapOf()
            for (path in lastStationPath) {
                val pathTo = data.getStation(path.to)
                if (closed.any { it.id == path.to } && pathTo != null) {
                    candidates[pathTo] = getDistance(end.location, pathTo.location)
                }
            }
            if(candidates.isEmpty()){
                gui.error(player,"逆算中に経路が途切れました。")
                logger.warning("[TransferGuideData] 候補経路先無し(${startId}->${endId}, closed: ${closed.joinToString{it.id}}, routeArrayList: ${routeArrayList.joinToString{it.id}}, candidates: ${candidates.toList().joinToString{it.first.id}})")
                return
            }
            val min = candidates.minByOrNull { it.value }
            if (min == null) {
                gui.error(player, "最小値からの駅探索Bに失敗しました。")
                logger.warning("[TransferGuideData] 最小値からの駅探索失敗B(${startId}->${endId}, closed: ${closed.joinToString{it.id}}, routeArrayList: ${routeArrayList.joinToString { it.id }}, candidates: ${candidates.toList().joinToString { it.first.id }})")
                return
            }
            routeArrayList.add(min.key)
            closed.remove(min.key)
            if (min.key.id == startId) {
                logger.info("[TransferGuide] step: B_END, routeArrayList: ${routeArrayList.joinToString { it.id }}")
                break@knitB
            }
            j++
        }
        routeArrayList.reverse()
        val routeArray = routeArrayList.toTypedArray()
        player.sendMessage(KRoute(data, routeArray).toString())
    }

    private fun openMainMenu() {
        gui.openMenu(
            player, "地点選択", listOf(
                MenuItem(
                    "出発地点:${data.getStation(startId)?.name ?: "未設定"}",
                    { chooseStation(StartOrEnd.START) },
                    Material.LIME_BANNER
                ),
                MenuItem(
                    "到着地点:${data.getStation(endId)?.name ?: "未設定"}",
                    { chooseStation(StartOrEnd.END) },
                    Material.RED_BANNER
                ),
                MenuItem("計算開始", { calcRoute() }, Material.COMMAND_BLOCK_MINECART),
                MenuItem("このアプリについて", { showAbout() }, Material.ENCHANTED_BOOK),
                MenuItem("終了", null, Material.BARRIER)
            )
        )
    }

    private fun showAbout() {
        player.sendMessage("Knit乗換案内\n製作者:Knit\nデータベース更新日:${data.update}\n未対応路線:新山吹村営鉄道(本線の薫風緑苑までのみ対応)\n仮の数値を使用している部分:新鮫、塩川、新スポーン地点-もさんな間の快速線\nまともにデバッグしていない為、ヤバいバグが発生する場合があります。ご了承下さい。")
    }

    private fun getKeyFromStationsMap(map: MutableMap<KStation, Double>, value: Double): KStation? {
        for (i in map) {
            if (i.value == value) return i.key
        }
        return null
    }

    private fun getDistance(start: DoubleArray, end: DoubleArray): Double {
        return abs(sqrt((start[0] - end[0]).pow(2.0) + (start[1] - end[1]).pow(2.0)))
    }
}

private class TransferGuideData {
    val stations: Map<String, KStation>
    val lines: Map<String, KLine>
    val directions: Map<String, String>
    val companies: Map<String, KCompany>
    val municipalities: Map<String, String>
    val loopMax: Int
    val update: String
    val consoleDebug: Boolean

    init {
        val conf = Config("transferGuideData").conf
        stations = stationsConfigToKStations(conf.getConfigurationSection("stations"))
        lines = linesConfigToKLines(conf.getConfigurationSection("lines"))
        directions = pairStringConfigToMap(conf.getConfigurationSection("directions"))
        companies = companiesConfigToKCompanies(conf.getConfigurationSection("companies"))
        municipalities = pairStringConfigToMap(conf.getConfigurationSection("municipalities"))
        loopMax = conf.getInt("loopMax")
        update = conf.getString("update") ?: "不明"
        consoleDebug = conf.getBoolean("consoleDebug", true)
    }

    fun getStation(id: String?): KStation? {
        return stations.getValue(id ?: return null)
    }

    private fun stationsConfigToKStations(conf: ConfigurationSection?): Map<String, KStation> {
        val map = mutableMapOf<String, KStation>()
        conf ?: return map
        val keys = conf.getKeys(false)
        for (key in keys) {
            val stationConfig = conf.getConfigurationSection(key)
            if (stationConfig != null) map[key] = KStation(stationConfig, key)
        }
        return map
    }

    private fun linesConfigToKLines(conf: ConfigurationSection?): Map<String, KLine> {
        val map = mutableMapOf<String, KLine>()
        conf ?: return map
        val keys = conf.getKeys(false)
        for (key in keys) {
            val lineConfig = conf.getConfigurationSection(key)
            if (lineConfig != null) map[key] = KLine(lineConfig)
        }
        return map
    }

    private fun companiesConfigToKCompanies(conf: ConfigurationSection?): Map<String, KCompany> {
        val map = mutableMapOf<String, KCompany>()
        conf ?: return map
        val keys = conf.getKeys(false)
        for (key in keys) {
            val section = conf.getConfigurationSection(key)
            if (section != null) map[key] = KCompany(section)
        }
        return map
    }

    private fun pairStringConfigToMap(conf: ConfigurationSection?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        conf ?: return map
        val keys = conf.getKeys(false)
        for (key in keys) {
            val str = conf.getString(key)
            if (str != null) map[key] = str
        }
        return map
    }
}

private class KStation(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name") ?: "null"
    val yomi = conf.getString("yomi") ?: "null"
    val number = conf.getString("number") ?: "null"
    val company: List<String> = conf.getStringList("company")
    val line: List<String> = conf.getStringList("line")
    val world = conf.getString("world") ?: "null"
    val location = conf.getDoubleList("location").toDoubleArray()
    val municipality: List<String> = conf.getStringList("municipality")
    val paths = pathsConfigToKPaths(conf.getConfigurationSection("paths"))
    private fun pathsConfigToKPaths(conf: ConfigurationSection?): MutableSet<KPath> {
        val set = mutableSetOf<KPath>()
        conf ?: return set
        val keys = conf.getKeys(false)
        for (key in keys) {
            val section = conf.getConfigurationSection(key)
            if (section != null) {
                val c = conf.getConfigurationSection(key)
                if (c != null) set.add(KPath(c))
            }
        }
        return set
    }
}

private class KPath(conf: ConfigurationSection) {
    val to = conf.getString("to") ?: "null"
    val line = conf.getString("line") ?: "null"
    val direction = conf.getString("direction") ?: "null"
    val time = conf.getInt("time")
}

private class KCompany(conf: ConfigurationSection) {
    val name = conf.getString("name")
    val lines: List<String> = conf.getStringList("lines")
}

private class KLine(conf: ConfigurationSection) {
    val name = conf.getString("name")
    val stations: List<String> = conf.getStringList("stations")
}

private class KRoute(val data: TransferGuideData, stations: Array<KStation>) {
    val routes: Array<KRouteBlock>

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
        //使用可能な移動経路群から使用する経路を選択(todo:コピペ部分多し、要改善)
        for (i in pathsCandidates.indices) {
            if (pathsCandidates.size <= 1) break
            if (pathsCandidates[i].size <= 1) continue
            if (i == 0) {//先頭の場合
                if (pathsCandidates[i + 1].size == 1) {//次の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val next = pathsCandidates[i + 1][0]
                        if (now is KRoutePathReal && next is KRoutePathReal && now.line == next.line && now.direction == next.direction) {//次の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                    //見つからなければ適当に
                    if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                } else {//次の路線が確定していない場合も適当
                    pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                }
            } else if (i == pathsCandidates.lastIndex) {//終端の場合
                if (pathsCandidates[i - 1].size == 1) {//前の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val before = pathsCandidates[i - 1][0]
                        if (now is KRoutePathReal && before is KRoutePathReal && now.line == before.line && now.direction == before.direction) {//前の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                    //見つからなければ適当に
                    if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                } else {//前の路線が確定していない場合も適当
                    pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                }
            } else {//中間部の場合
                if (pathsCandidates[i + 1].size == 1) {//次の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val next = pathsCandidates[i + 1][0]
                        if (now is KRoutePathReal && next is KRoutePathReal && now.line == next.line && now.direction == next.direction) {//次の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                    //見つからなければ適当に
                    if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                } else if (pathsCandidates[i - 1].size == 1) {//前の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val before = pathsCandidates[i - 1][0]
                        if (now is KRoutePathReal && before is KRoutePathReal && now.line == before.line && now.direction == before.direction) {//前の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                    //見つからなければ適当に
                    if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                } else {//前後の路線が確定していない場合も適当
                    pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                }
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

    override fun toString(): String {
        val sb = StringBuilder()
        var appendTime = 0
        sb.append("===== 結果 =====\n")
        for (route in routes) {
            sb.append(
                "${route.station.name}(${route.station.yomi}${
                    if (route.station.number != "null") {
                        "/${route.station.number}"
                    } else {
                        ""
                    }
                }/X:${route.station.location[0]},Z:${route.station.location[1]})\n"
            )
            if (route.routePath is KRoutePathReal) {
                appendTime += route.routePath.time + if (route.routePath.line == "walk") {
                    10
                } else {
                    30
                }
                sb.append(" | ${data.lines[route.routePath.line]?.name ?: route.routePath.line}${data.directions[route.routePath.direction]} 約${route.routePath.time}秒\n")
            }
        }
        sb.append("所要時間:約${appendTime}秒\n")
        sb.append("================")
        return sb.toString()
    }
}

private class KRouteBlock(
    val station: KStation,
    val routePath: KRoutePath
)

private abstract class KRoutePath
private class KRoutePathReal(
    val line: String,
    val direction: String,
    var time: Int,
) : KRoutePath()

private open class KRoutePathEnd : KRoutePath()

private enum class StartOrEnd {
    START, END
}

private enum class JapaneseColumns(val firstChar: String, val chars: Array<String>) {
    A("あ", arrayOf("あ", "い", "う", "え", "お")),
    KA("か", arrayOf("か", "き", "く", "け", "こ", "が", "ぎ", "ぐ", "げ", "ご")),
    SA("さ", arrayOf("さ", "し", "す", "せ", "そ", "ざ", "じ", "ず", "ぜ", "ぞ")),
    TA("た", arrayOf("た", "ち", "つ", "て", "と", "だ", "ぢ", "づ", "で", "ど")),
    NA("な", arrayOf("な", "に", "ぬ", "ね", "の")),
    HA("は", arrayOf("は", "ひ", "ふ", "へ", "ほ", "ば", "び", "ぶ", "べ", "ぼ", "ぱ", "ぴ", "ぷ", "ぺ", "ぽ")),
    MA("ま", arrayOf("ま", "み", "む", "め", "も")),
    YA("や", arrayOf("や", "ゆ", "よ")),
    RA("ら", arrayOf("ら", "り", "る", "れ", "ろ")),
    WA("わ", arrayOf("わ", "ゐ", "ゑ", "を", "ん")),
}