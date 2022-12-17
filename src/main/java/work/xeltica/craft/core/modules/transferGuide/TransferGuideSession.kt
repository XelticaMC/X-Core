package work.xeltica.craft.core.modules.transferGuide

import java.io.IOException
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.transferGuide.dataElements.KCompany
import work.xeltica.craft.core.modules.transferGuide.dataElements.KLine
import work.xeltica.craft.core.modules.transferGuide.dataElements.KMuni
import work.xeltica.craft.core.modules.transferGuide.dataElements.KStation
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData
import work.xeltica.craft.core.modules.transferGuide.enums.JapaneseColumns
import work.xeltica.craft.core.modules.transferGuide.enums.StationChoiceTarget
import work.xeltica.craft.core.modules.transferGuide.routeElements.KRoute
import work.xeltica.craft.core.utils.Config

class TransferGuideSession(val player: Player) {
    private val data = TransferGuideData()
    private val gui = Gui.getInstance()
    private val logger = Bukkit.getLogger()
    private var startId: String? = null
    private var endId: String? = null
    private var infoId: String? = null

    companion object {
        @JvmStatic
        fun verifyData(): Boolean {
            val data = TransferGuideData()
            val logger = Bukkit.getLogger()
            var count = 0
            data.stations.forEach { station ->
                station.value.paths.forEach { path ->
                    if (!data.stationExists(path.to)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${path.to}(stations.${station.key})")
                        count++
                    }
                    if (path.line != "walk" && !data.lineExists(path.line)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない路線ID:${path.line}(stations.${station.key})")
                        count++
                    }
                    if (!data.directionExists(path.direction)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない方向ID:${path.line}(stations.${station.key})")
                    }
                    if (path.time <= 0) {
                        logger.warning("[TransferGuideData(verifyData)] 無効な所要時間:${path.time}(stations.${station.key})")
                        count++
                    }
                }
            }
            data.lines.forEach { line ->
                line.value.stations.forEach { station ->
                    if (!data.stationExists(station)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${station}(lines.${line.key}.stations)")
                        count++
                    }
                }
            }
            data.companies.forEach { company ->
                company.value.lines.forEach { line ->
                    if (!data.lineExists(line)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない路線ID:${line}(companies.${company.key}.lines)")
                        count++
                    }
                }
            }
            data.municipalities.forEach { municipality ->
                municipality.value.stations.forEach { station ->
                    if (!data.stationExists(station)) {
                        logger.warning("[TransferGuideData(verifyData)] 存在しない駅ID:${station}(municipalities.${municipality.key}.stations)")
                        count++
                    }
                }
            }
            if (count > 0) logger.warning("[TransferGuideData(verifyData)] ${count}個のデータ誤りが見つかりました。")
            else logger.info("[TransferGuideData(verifyData)] データに誤りは見つかりませんでした。")
            return count == 0
        }
    }

    init {
        val userData = Config("transferGuideUserData").conf.getConfigurationSection(player.uniqueId.toString())
        userData?.getString("start")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) startId = this
        }
        userData?.getString("end")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) endId = this
        }
        userData?.getString("info")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) infoId = this
        }
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
        data.getStationsInWorld(player.world.name)
            .filter { it.yomi.first().toString() == char }
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
            if (line == null) {
                logger.warning("[TransferGuideData] 存在しない路線ID:${lineId}(${company.name}内)")
                return@forEach
            }
            if (line.world == player.world.name) {
                items.add(
                    MenuItem(
                        line.name,
                        { chooseStationLine(stationChoiceTarget, company, line) },
                        Material.RAIL
                    )
                )
            }
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, company.name, items)
    }

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget, company: KCompany, line: KLine) {
        val items = ArrayList<MenuItem>()
        line.stations.forEach {
            val station = data.stations[it]
            if (station == null) {
                logger.warning("[TransferGuideData] 存在しない駅ID:${it}(${line.name}内)")
                return@forEach
            }
            if (station.world == player.world.name) {
                items.add(
                    MenuItem(
                        station.name,
                        { setStationIdAndOpenMenu(station.id, stationChoiceTarget) },
                        Material.MINECART
                    )
                )
            }
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
            if (station == null) {
                logger.warning("[TransferGuideData] 存在しない駅ID:${stationId}(${muni.id}内)")
                return@forEach
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
        data.getStationsInWorld(player.world.name).forEach {
            val distance =
                TransferGuideUtil.calcDistance(doubleArrayOf(player.location.x, player.location.z), it.location)
            stations[distance] = it
        }
        val distances = stations.keys.sorted()
        val items = ArrayList<MenuItem>()
        for (i in 0..15) {
            try {
                val station = stations.getValue(distances[i])
                items.add(
                    MenuItem(
                        "${station.name}(約${TransferGuideUtil.metersToString(distances[i])})",
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
        data.getStationsInWorld(player.world.name).forEach { station ->
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
        val start = data.stations[startId]
        if (start == null) {
            gui.error(player, "出発地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${startId}")
            return
        }
        val end = data.stations[endId]
        if (end == null) {
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
        opened[start] = TransferGuideUtil.calcDistance(start.location, end.location)
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
            if (minStationEntry == null) {
                gui.error(player, "最小値からの駅探索Aに失敗しました。")
                logger.warning("[TransferGuide] 最小値からの駅探索失敗A\n${loopADebugString()}")
                return
            }
            closed.add(minStationEntry.key)
            opened.remove(minStationEntry.key)
            for (path in minStationEntry.key.paths) {
                val pathToInUnsearched = unsearched[path.to] ?: continue
                opened[pathToInUnsearched] = TransferGuideUtil.calcDistance(end.location, pathToInUnsearched.location)
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
                if (closed.any { it.id == path.to }) candidates[pathTo] =
                    TransferGuideUtil.calcDistance(end.location, pathTo.location)
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
        player.sendMessage(KRoute(data, routeArray).toStringForGuide())
    }

    private fun openStationInfoMenu() {
        val items = ArrayList<MenuItem>()
        if (infoId != null) {
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
        val station = data.stations[infoId]
        if (station == null) {
            gui.error(player, "存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${infoId}")
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
                TransferGuideUtil.metersToString(
                    TransferGuideUtil.calcDistance(
                        doubleArrayOf(
                            player.location.x,
                            player.location.z
                        ), station.location
                    )
                )
            })\n"
        )
        sb.append("会社:${companies.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("路線:${lines.joinToString(separator = "、", transform = { it.value.name })}\n")
        sb.append("近隣自治体:")
        if (municipalities.isEmpty()) sb.append("無し\n")
        else sb.append("近隣自治体:${municipalities.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("隣の駅:\n")
        station.paths.forEach {
            sb.append(" ${it.toStringForGuide(data)}\n")
        }
        sb.append("================")
        player.sendMessage(sb.toString())
    }
}