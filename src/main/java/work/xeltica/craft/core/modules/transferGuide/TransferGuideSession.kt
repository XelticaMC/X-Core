package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class TransferGuideSession(val player: Player) {
    private val data = TransferGuideData()
    private val gui = Gui.getInstance()
    private val logger = Bukkit.getLogger()
    private var startId: String? = null
    private var endId: String? = null
    private var infoId: String? = null

    init {
        val userData = Config("transferGuideUserData").conf.getConfigurationSection(player.uniqueId.toString())
        userData?.getString("start")?.run { startId = this }
        userData?.getString("end")?.run { endId = this }
        userData?.getString("info")?.run { infoId = this }
    }

    private fun setStationIdAndOpenMenu(newId: String, stationChoiceTarget: StationChoiceTarget) {
        when (stationChoiceTarget) {
            StationChoiceTarget.START -> startId = newId
            StationChoiceTarget.END -> endId = newId
            StationChoiceTarget.INFO -> {
                infoId = newId
                saveUserData()
                showStationData()
                return
            }
        }
        saveUserData()
        openMainMenu()
    }

    private fun saveUserData() {
        try {
            val userData = Config("transferGuideUserData")
            val uuid = player.uniqueId.toString()
            userData.conf.set("${uuid}.start", startId)
            userData.conf.set("${uuid}.end", endId)
            userData.conf.set("${uuid}.info", infoId)
            userData.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() {
        openMainMenu()
    }

    private fun chooseStation(stationChoiceTarget: StationChoiceTarget) {
        when (player.world.name) {
            "main" -> chooseStationMain(stationChoiceTarget)
            "wildarea2", "wildarea2_nether" -> chooseStationWild(stationChoiceTarget)
            else -> gui.error(player, "今いるワールドには鉄道は登録されていません！")
        }
    }

    private fun chooseStationMain(stationChoiceTarget: StationChoiceTarget) {
        val items = arrayListOf(
            MenuItem("近い順", { chooseStationNear(stationChoiceTarget) }, Material.DIAMOND_HORSE_ARMOR),
            MenuItem("五十音順", { chooseStationAiueo(stationChoiceTarget) }, Material.BOOK),
            MenuItem("会社・路線別", { chooseStationLine(stationChoiceTarget) }, Material.SPRUCE_DOOR),
            MenuItem("最寄自治体別", { chooseStationMuni(stationChoiceTarget) }, Material.FILLED_MAP),
        )
        when (stationChoiceTarget) {
            StationChoiceTarget.START, StationChoiceTarget.END -> items.add(
                MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH)
            )

            StationChoiceTarget.INFO -> items.add(
                MenuItem("戻る", { openStationInfoMenu() }, Material.REDSTONE_TORCH)
            )
        }
        gui.openMenu(player, "駅選択", items)
    }

    private fun chooseStationAiueo(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        JapaneseColumns.values().forEach { column ->
            items.add(
                MenuItem("${column.firstChar}行", { chooseStationAiueo(stationChoiceTarget, column) }, Material.BOOK)
            )
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "五十音順", items)
    }

    private fun chooseStationAiueo(stationChoiceTarget: StationChoiceTarget, column: JapaneseColumns) {
        val items = ArrayList<MenuItem>()
        column.chars.forEach { char ->
            items.add(
                MenuItem(char, { chooseStationAiueo(stationChoiceTarget, column, char) }, Material.BOOK)
            )
        }
        items.add(MenuItem("戻る", { chooseStationAiueo(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${column.firstChar}行", items)
    }

    private fun chooseStationAiueo(stationChoiceTarget: StationChoiceTarget, column: JapaneseColumns, char: String) {
        val stations = ArrayList<KStation>()
        data.stations.values
            .filter { it.world == player.world.name && it.yomi.first().toString() == char }
            .forEach { stations.add(it) }
        val items = ArrayList<MenuItem>()
        stations
            .sortedBy { it.yomi }
            .forEach { station ->
                items.add(
                    MenuItem(
                        station.name,
                        { setStationIdAndOpenMenu(station.id, stationChoiceTarget) },
                        Material.MINECART
                    )
                )
            }
        items.add(MenuItem("戻る", { chooseStationAiueo(stationChoiceTarget, column) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${char}から始まる駅一覧", items)
    }

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        data.companies.values.forEach { company ->
            items.add(MenuItem(company.name, { chooseStationLine(stationChoiceTarget, company) }, Material.SPRUCE_DOOR))
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "会社選択", items)
    }

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget, company: KCompany) {
        val items = ArrayList<MenuItem>()
        company.lines.forEach { lineId ->
            val line = data.lines[lineId]
            line ?: run {
                logger.warning("[TransferGuideData] 存在しない路線ID(${company.name}内)")
                return
            }
            items.add(
                MenuItem(
                    line.name,
                    { chooseStationLine(stationChoiceTarget, company, line) },
                    Material.RAIL
                )
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, company.name, items)
    }

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget, company: KCompany, line: KLine) {
        val items = ArrayList<MenuItem>()
        line.stations.forEach {
            val station = data.stations[it] ?: run {
                logger.warning("[TransferGuideData] 存在しない駅ID(${line.name}内)")
                return
            }
            if (station.world == player.world.name) items.add(
                MenuItem(
                    station.name,
                    { setStationIdAndOpenMenu(station.id, stationChoiceTarget) },
                    Material.MINECART
                )
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget, company) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, line.name, items)
    }

    private fun chooseStationMuni(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        data.municipalities.values
            .filter { it.world == player.world.name }
            .forEach { muni ->
                items.add(
                    MenuItem(muni.name, { chooseStationMuni(stationChoiceTarget, muni) }, Material.FILLED_MAP)
                )
            }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "自治体選択", items)
    }

    private fun chooseStationMuni(stationChoiceTarget: StationChoiceTarget, muni: KMuni) {
        val items = ArrayList<MenuItem>()
        muni.stations.forEach { stationId ->
            val station = data.stations[stationId]
            station ?: run {
                logger.warning("[TransferGuideData] 存在しない駅ID(${muni.id}内)")
                return
            }
            items.add(
                MenuItem(station.name, { setStationIdAndOpenMenu(stationId, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStationMuni(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${data.municipalities[muni.id]?.name}", items)
    }

    private fun chooseStationNear(stationChoiceTarget: StationChoiceTarget) {
        val stations: MutableMap<Double, KStation> = mutableMapOf()
        data.stations.values
            .filter { it.world == player.world.name }
            .forEach {
                val distance = getDistance(doubleArrayOf(player.location.x, player.location.z), it.location)
                stations[distance] = it
            }

        val distances = stations.keys.sorted()
        val items = ArrayList<MenuItem>()
        for (i in 0..15) {
            try {
                val station = stations.getValue(distances[i])
                items.add(
                    MenuItem(
                        "${station.name}(約${distances[i].toInt()}m)",
                        { setStationIdAndOpenMenu(station.id, stationChoiceTarget) },
                        Material.MINECART
                    )
                )
            } catch (_: Exception) {
            }
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "近い順", items)
    }

    private fun chooseStationWild(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        data.stations.values
            .filter { it.world == player.world.name }
            .forEach { station ->
                items.add(
                    MenuItem(
                        station.name,
                        { setStationIdAndOpenMenu(station.id, stationChoiceTarget) },
                        Material.MINECART
                    )
                )
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
        val start = data.stations[startId] ?: run {
            gui.error(player, "出発地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${startId}")
            return
        }
        val end = data.stations[endId] ?: run {
            gui.error(player, "到着地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${endId}")
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
        fun loopADebugString(): String {
            return "step=A${i}\n" +
                    "route=${startId}->${endId}\n" +
                    "unsearched=${unsearched.toList().joinToString { it.first }}\n" +
                    "opened=${opened.toList().joinToString { it.first.id }}\n" +
                    "closed=${closed.joinToString { it.id }}"
        }
        knitA@ while (i < data.loopMax) {
            if (data.consoleDebug) logger.info("[TransferGuide(debug)] ${loopADebugString()}")
            val minStationEntry = opened.minByOrNull { it.value }
            minStationEntry ?: run {
                gui.error(player, "最小値からの駅探索Aに失敗しました。")
                logger.warning("[TransferGuide] 最小値からの駅探索失敗A\n${loopADebugString()}")
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
                    if (data.consoleDebug) logger.info("[TransferGuide(debug)] A_END\n${loopADebugString()}")
                    break@knitA
                }
            }
            i++
        }
        val routeArrayList = ArrayList<KStation>()
        routeArrayList.add(end)
        closed.removeIf { it.id == endId }
        var j = 0
        fun loopBDebugString(): String {
            return "step=B${j}\n" +
                    "route=${startId}->${endId}\n" +
                    "closed=${closed.joinToString { it.id }}\n" +
                    "routeArrayList=${routeArrayList.joinToString { it.id }}"
        }
        knitB@ while (j < data.loopMax) {
            if (data.consoleDebug) logger.info("[TransferGuideData(debug)] ${loopBDebugString()}")
            val lastStationPath = routeArrayList.last().paths
            val candidates: MutableMap<KStation, Double> = mutableMapOf()
            lastStationPath.forEach { path ->
                val pathTo = data.stations[path.to] ?: run {
                    gui.error(player, "[TransferGuideData] 存在しない駅ID(${path.to})")
                    return@forEach
                }
                if (closed.any { it.id == path.to }) candidates[pathTo] = getDistance(end.location, pathTo.location)
            }
            if (candidates.isEmpty()) {
                gui.error(player, "逆算中に経路が途切れました。")
                logger.warning("[TransferGuideData] 候補経路先無しB\n${loopBDebugString()}")
                return
            }
            val min = candidates.minByOrNull { it.value } ?: run {
                gui.error(player, "最小値からの駅探索Bに失敗しました。")
                logger.warning("[TransferGuideData] 最小値からの駅探索失敗B\n${loopBDebugString()}")
                return
            }
            routeArrayList.add(min.key)
            closed.remove(min.key)
            if (min.key.id == startId) {
                if (data.consoleDebug) logger.info("[TransferGuide] step: B_END\n${loopBDebugString()}")
                break@knitB
            }
            j++
        }
        routeArrayList.reverse()
        val routeArray = routeArrayList.toTypedArray()
        player.sendMessage(KRoute(data, routeArray).toString())
    }

    private fun openStationInfoMenu() {
        val items = ArrayList<MenuItem>()
        infoId?.run {
            items.add(
                MenuItem(
                    "前回見た駅:${data.stations[infoId]?.name}",
                    { showStationData() },
                    Material.CLOCK
                )
            )
        }
        items.add(MenuItem("駅選択", { chooseStation(StationChoiceTarget.INFO) }, Material.COMPASS))
        items.add(MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "駅情報", items)
    }

    private fun openMainMenu() {
        gui.openMenu(
            player, "地点選択", listOf(
                MenuItem(
                    "出発地点:${data.stations[startId]?.name ?: "未設定"}",
                    { chooseStation(StationChoiceTarget.START) },
                    Material.LIME_BANNER
                ),
                MenuItem(
                    "到着地点:${data.stations[endId]?.name ?: "未設定"}",
                    { chooseStation(StationChoiceTarget.END) },
                    Material.RED_BANNER
                ),
                MenuItem("駅情報", { openStationInfoMenu() }, Material.CHEST_MINECART),
                MenuItem("計算開始", { calcRoute() }, Material.COMMAND_BLOCK_MINECART),
                MenuItem("このアプリについて", { showAbout() }, Material.ENCHANTED_BOOK),
                MenuItem("終了", null, Material.BARRIER)
            )
        )
    }

    private fun showAbout() {
        player.sendMessage("Knit乗換案内\n製作者:Knit\nデータベース更新日:${data.update}\n未対応路線:新山吹村営鉄道(本線の薫風緑苑までのみ対応)\n仮の数値を使用している部分:新鮫、塩川、新スポーン地点-もさんな間の快速線\nまともにデバッグしていない為、ヤバいバグが発生する場合があります。ご了承下さい。")
    }

    private fun showStationData() {
        val sb = StringBuilder()
        val station = data.stations[infoId] ?: run {
            gui.error(player, "存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID(${infoId})")
            return
        }
        val lines = ArrayList<Map.Entry<String, KLine>>()
        data.lines
            .filter { it.value.stations.contains(infoId) }
            .forEach {
                lines.add(it)
            }
        val companies = ArrayList<KCompany>()
        data.companies.values
            .forEach { company ->
                lines.forEach {
                    if (!(companies.contains(company)) && company.lines.contains(it.key)) companies.add(company)
                }
            }
        val municipalities = data.municipalities.values.filter { it.stations.contains(infoId) }
        sb.append("===== ${station.name}駅 =====\n")
        sb.append("読み:${station.yomi}\n")
        sb.append("駅番号:")
        if (station.number == null) sb.append("無し\n")
        else sb.append("${station.number}\n")
        sb.append(
            "座標:[X:${station.location[0]},Z:${station.location[1]}]付近(ここから約${
                getDistance(
                    doubleArrayOf(
                        player.location.x,
                        player.location.z
                    ), station.location
                ).toInt()
            }m)\n"
        )
        sb.append("会社:${companies.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("路線:${lines.joinToString(separator = "、", transform = { it.value.name })}\n")
        sb.append("近隣自治体:${municipalities.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("隣の駅:\n")
        station.paths.forEach {
            if (it.line == "walk") sb.append(" ${data.stations[it.to]?.name}:${data.directions[it.direction]}約${it.time}秒歩く\n")
            else sb.append(" ${data.stations[it.to]?.name}:${data.lines[it.line]?.name}(${data.directions[it.direction]})約${it.time}秒\n")
        }
        sb.append("================")
        player.sendMessage(sb.toString())
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
    val municipalities: Map<String, KMuni>
    val loopMax: Int
    val update: LocalDateTime
    val consoleDebug: Boolean

    init {
        val conf = Config("transferGuideData").conf
        stations = stationsConfigToKStations(conf.getConfigurationSection("stations"))
        lines = linesConfigToKLines(conf.getConfigurationSection("lines"))
        directions = pairStringConfigToMap(conf.getConfigurationSection("directions"))
        companies = companiesConfigToKCompanies(conf.getConfigurationSection("companies"))
        municipalities = munisConfigToKMunis(conf.getConfigurationSection("municipalities"))
        loopMax = conf.getInt("loopMax")
        update = LocalDateTime.parse(conf.getString("update"))
        consoleDebug = conf.getBoolean("consoleDebug", false)
    }

    private fun stationsConfigToKStations(conf: ConfigurationSection?): Map<String, KStation> {
        val map = mutableMapOf<String, KStation>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KStation(this, key) }
        }
        return map.toMap()
    }

    private fun linesConfigToKLines(conf: ConfigurationSection?): Map<String, KLine> {
        val map = mutableMapOf<String, KLine>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KLine(this) }
        }
        return map
    }

    private fun companiesConfigToKCompanies(conf: ConfigurationSection?): Map<String, KCompany> {
        val map = mutableMapOf<String, KCompany>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KCompany(this) }
        }
        return map.toMap()
    }

    private fun munisConfigToKMunis(conf: ConfigurationSection?): Map<String, KMuni> {
        val map = mutableMapOf<String, KMuni>()
        conf ?: return map
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { map[key] = KMuni(this, key) }
        }
        return map.toMap()
    }

    private fun pairStringConfigToMap(conf: ConfigurationSection?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        conf ?: return map
        conf.getKeys(false).forEach {
            conf.getString(it)?.run { map[it] = this }
        }
        return map.toMap()
    }
}

private class KStation(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name") ?: "null"
    val yomi = conf.getString("yomi") ?: "null"
    val number = conf.getString("number")
    val world = conf.getString("world") ?: "null"
    val location = conf.getDoubleList("location").toDoubleArray()
    val paths = pathsConfigToKPaths(conf.getConfigurationSection("paths"))
    private fun pathsConfigToKPaths(conf: ConfigurationSection?): MutableSet<KPath> {
        val set = mutableSetOf<KPath>()
        conf ?: return set
        conf.getKeys(false).forEach { key ->
            conf.getConfigurationSection(key)?.run { set.add(KPath(this)) }
        }
        return set
    }
}

private class KMuni(conf: ConfigurationSection, val id: String) {
    val name = conf.getString("name") ?: "null"
    val world = conf.getString("world")
    val stations: List<String> = conf.getStringList("stations")
}

private class KPath(conf: ConfigurationSection) {
    val to = conf.getString("to") ?: "null"
    val line = conf.getString("line") ?: "null"
    val direction = conf.getString("direction") ?: "null"
    val time = conf.getInt("time")
}

private class KCompany(conf: ConfigurationSection) {
    val name = conf.getString("name") ?: "null"
    val lines: List<String> = conf.getStringList("lines")
}

private class KLine(conf: ConfigurationSection) {
    val name = conf.getString("name") ?: "null"
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
        //使用可能な移動経路群から使用する経路を選択
        for (i in pathsCandidates.indices) {
            if (pathsCandidates.size <= 1) break
            if (pathsCandidates[i].size <= 1) continue
            fun isBeforeDecided(): Boolean {
                val beforeDecided = pathsCandidates[i - 1].size == 1
                if (beforeDecided) {//前の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val before = pathsCandidates[i - 1][0]
                        if (now is KRoutePathReal && before is KRoutePathReal && now.line == before.line && now.direction == before.direction) {//前の路線と同じものを検索
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
                if (nextDecided) {//次の路線が確定しているならば
                    for (now in pathsCandidates[i]) {
                        val next = pathsCandidates[i + 1][0]
                        if (now is KRoutePathReal && next is KRoutePathReal && now.line == next.line && now.direction == next.direction) {//次の路線と同じものを検索
                            pathsCandidates[i] = arrayListOf(now)
                            break
                        }
                    }
                }
                //見つからなければ適当に
                if (pathsCandidates[i].size >= 2) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])
                return nextDecided
            }
            if (i == 0) {//先頭の場合
                if (!isNextDecided()) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])//次の路線が確定していなければ適当に
            } else if (i == pathsCandidates.lastIndex) {//終端の場合
                if (!isBeforeDecided()) pathsCandidates[i] = arrayListOf(pathsCandidates[i][0])//前の路線が確定していなければ適当に
            } else {//中間部の場合
                if (!(isNextDecided() && isBeforeDecided())) pathsCandidates[i] =
                    arrayListOf(pathsCandidates[i][0])//前後の路線が確定していない場合は適当に
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
        routes.forEach {
            sb.append("${it.station.name}(${it.station.yomi}")
            it.station.number?.run { sb.append("/${it.station.number}") }
            sb.append("/X:${it.station.location[0]},Z:${it.station.location[1]})\n")
            if (it.routePath is KRoutePathReal) {
                appendTime += it.routePath.time
                appendTime += if (it.routePath.line == "walk") 10 else 30
                sb.append(" | ")
                if (it.routePath.line == "walk") sb.append("${data.directions[it.routePath.direction]}約${it.routePath.time}秒歩く\n")
                else sb.append("${data.lines[it.routePath.line]?.name ?: it.routePath.line}(${data.directions[it.routePath.direction]}) 約${it.routePath.time}秒\n")
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

private enum class StationChoiceTarget {
    START, END, INFO
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