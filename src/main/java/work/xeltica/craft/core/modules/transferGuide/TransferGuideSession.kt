package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.transferGuide.dataElements.KCompany
import work.xeltica.craft.core.modules.transferGuide.dataElements.KLine
import work.xeltica.craft.core.modules.transferGuide.dataElements.KLines
import work.xeltica.craft.core.modules.transferGuide.dataElements.KMuni
import work.xeltica.craft.core.modules.transferGuide.dataElements.KMunis
import work.xeltica.craft.core.modules.transferGuide.dataElements.KStation
import work.xeltica.craft.core.modules.transferGuide.dataElements.KStations
import work.xeltica.craft.core.modules.transferGuide.dataElements.TransferGuideData
import work.xeltica.craft.core.modules.transferGuide.enums.JapaneseColumns
import work.xeltica.craft.core.modules.transferGuide.enums.StationChoiceTarget
import work.xeltica.craft.core.modules.transferGuide.routeElements.KRoute
import java.io.IOException

/**
 * プレイヤーがアプリを開いてから閉じるまでの一連の流れ(セッション)を表します。
 * @author Knit prg.
 */

class TransferGuideSession(val player: Player) {
    private val data = TransferGuideData()
    private val gui = Gui.getInstance()
    private val logger = Bukkit.getLogger()
    private var startId: String? = null
    private var endId: String? = null
    private var infoId: String? = null

    init {
        val userData = Config("transferGuideUserData").conf.getConfigurationSection(player.uniqueId.toString())
        userData?.getString("${player.world.name}.start")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) startId = this
        }
        userData?.getString("${player.world.name}.end")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) endId = this
        }
        userData?.getString("${player.world.name}.info")?.run {
            if (data.stationExists(this) && data.isStationInWorld(this, player.world.name)) infoId = this
        }
    }

    /**
     * セッションを開始します。
     */
    fun start() {
        openMainMenu()
    }

    /**
     * 駅を設定してメインメニューに戻ります。
     */

    private fun setStationIdAndOpenMainMenu(newId: String, stationChoiceTarget: StationChoiceTarget) {
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

    /**
     * ユーザーが選択した駅を保存します。
     */

    private fun saveUserData() {
        try {
            val userData = Config("transferGuideUserData")
            val uuid = player.uniqueId.toString()
            userData.conf.set("${uuid}.${player.world.name}.start", startId)
            userData.conf.set("${uuid}.${player.world.name}.end", endId)
            userData.conf.set("${uuid}.${player.world.name}.info", infoId)
            userData.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * GUI: メインメニュー
     */

    private fun openMainMenu() {
        gui.openMenu(
            player, "乗換案内メインメニュー", listOf(
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
                MenuItem("計算開始", { calcRoute() }, Material.COMMAND_BLOCK_MINECART),
                MenuItem("駅情報", { openStationInfoMenu() }, Material.CHEST_MINECART),
                MenuItem("このアプリについて", { showAbout() }, Material.ENCHANTED_BOOK),
                MenuItem("終了", null, Material.BARRIER)
            )
        )
    }

    /**
     * GUI: 駅選択
     * ワールドに応じて適した駅選択画面を開きます。
     */

    private fun chooseStation(stationChoiceTarget: StationChoiceTarget) {
        when (data.availableWorlds[player.world.name]) {
            "main" -> chooseStationMain(stationChoiceTarget)
            "wild" -> chooseStationWild(stationChoiceTarget)
            else -> gui.error(player, "今いるワールドには鉄道は登録されていません！")
        }
    }

    /**
     * GUI: 駅選択
     * メインワールド等の駅や路線が多いワールドに適した駅選択画面です。
     */

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

    /**
     * GUI: 駅選択/五十音順
     */

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

    /**
     * GUI: 駅選択/五十音順/○行
     */

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

    /**
     * GUI: 駅選択/五十音順/○行/□から始まる駅一覧
     */

    private fun chooseStationAiueo(stationChoiceTarget: StationChoiceTarget, column: JapaneseColumns, char: String) {
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
            .filterByYomiInitial(char)
            .sortByYomi()
        val items = ArrayList<MenuItem>()
        stations.value.forEach { station ->
            items.add(
                MenuItem(station.second.name, { setStationIdAndOpenMainMenu(station.first, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStationAiueo(stationChoiceTarget, column) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${char}から始まる駅一覧", items)
    }

    /**
     * GUI: 駅選択/会社選択
     */

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        data.companies.values.forEach { company ->
            items.add(MenuItem(company.name, { chooseStationLine(stationChoiceTarget, company) }, Material.SPRUCE_DOOR))
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "会社選択", items)
    }

    /**
     * GUI: 駅選択/会社選択/(会社)
     */

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget, company: KCompany) {
        val items = ArrayList<MenuItem>()
        val lines = KLines.allLines(data)
            .filterByWorld(player.world.name)
            .filterByCompany(company)
        lines.value.forEach { line ->
            items.add(
                MenuItem(line.second.name, { chooseStationLine(stationChoiceTarget, company, line.second) }, Material.RAIL)
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, company.name, items)
    }

    /**
     * GUI: 駅選択/会社選択/(会社)/(路線)
     */

    private fun chooseStationLine(stationChoiceTarget: StationChoiceTarget, company: KCompany, line: KLine) {
        val items = ArrayList<MenuItem>()
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
            .filterByLine(line)
        stations.value.forEach { station ->
            items.add(
                MenuItem(station.second.name, { setStationIdAndOpenMainMenu(station.first, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget, company) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, line.name, items)
    }

    /**
     * GUI: 駅選択/自治体選択
     */

    private fun chooseStationMuni(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        val munis = KMunis.allMunis(data)
            .filterByWorld(player.world.name)
        munis.value.forEach { muni ->
            items.add(
                MenuItem(muni.second.name, { chooseStationMuni(stationChoiceTarget, muni.second) }, Material.FILLED_MAP)
            )
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "自治体選択", items)
    }

    /**
     * GUI: 駅選択/自治体選択/(自治体)
     */

    private fun chooseStationMuni(stationChoiceTarget: StationChoiceTarget, muni: KMuni) {
        val items = ArrayList<MenuItem>()
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
            .filterByMuni(muni)
        stations.value.forEach { station ->
            items.add(
                MenuItem(station.second.name, { setStationIdAndOpenMainMenu(station.first, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStationMuni(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "${data.municipalities[muni.id]?.name}", items)
    }

    /**
     * GUI: 駅選択/近い順
     */

    private fun chooseStationNear(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        val playerLocation = doubleArrayOf(player.location.x, player.location.z)
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
            .sortByDistance(playerLocation)
            .fromBegin(16)
        stations.value.forEach { station ->
            val distance = TransferGuideUtil.metersToString(TransferGuideUtil.calcDistance(playerLocation, station.second.location))
            items.add(
                MenuItem("${station.second.name}(約${distance})", { setStationIdAndOpenMainMenu(station.first, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "近い順", items)
    }

    /**
     * GUI: 駅選択
     * ワイルドエリア等の散発的に少数の路線が存在するワールドに適した駅選択画面です。
     */

    private fun chooseStationWild(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
        stations.value.forEach { station ->
            items.add(
                MenuItem(station.second.name, { setStationIdAndOpenMainMenu(station.first, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "駅一覧", items)
    }

    /**
     * 選択した駅からルートを検索します。
     */

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
            A*っぽいもののつもりだけどちゃんと実装できているかは不明
            1. 全ての駅を表すカードが裏返しで置いてあると想定する。(unsearched)
            2. 出発地点の駅のカードを開ける。(unsearchedから削除、openedへ追加)
            3. 既に開いているカードの内、最も到着地点の駅への直線距離が短い駅(minStation)の隣の駅を全て開け(openedへ追加)、
               その駅に印をつける(closedに追加しopenedから削除)。
            4. 開いたカードに到着地点の駅が含まれていなければ3. へ戻る。(Aループ)
            5. 印をつけた駅を結んで、おわり。(Bループ)
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
            opened.forEach { openedEntry ->
                var distance = TransferGuideUtil.calcDistance(end.location, openedEntry.key.location)
                if (closed.isNotEmpty()) distance -= TransferGuideUtil.calcDistance(
                    closed.last().location,
                    openedEntry.key.location
                )
                if (TransferGuideUtil.containsSamePath(openedEntry.key.paths, end.paths)) distance /= 2
                opened[openedEntry.key] = distance
            }
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
            if (data.consoleDebug) {
                logger.info("[TransferGuideData(debug)] ${loopBDebugString()}")
            }
            val lastStationPath = routeArrayList.last().paths
            val candidates: MutableMap<KStation, Double> = mutableMapOf()
            lastStationPath.forEach { path ->
                val pathTo = data.stations[path.to] ?: run {
                    gui.error(player, "[TransferGuideData] 存在しない駅ID(${path.to})")
                    return@forEach
                }
                if (closed.any { it.id == path.to }) {
                    candidates[pathTo] =
                        TransferGuideUtil.calcDistance(end.location, pathTo.location)
                }
            }
            if (candidates.isEmpty()) {
                var k = 0
                while (k < data.loopMax) {
                    if (data.consoleDebug) {
                        logger.info("[TransferGuideData(debug)] step=Ba${k}\n${loopBDebugString()}")
                    }
                    for (path in routeArrayList.last().paths) {
                        if (closed.any { it.id == path.to }) continue@knitB
                    }
                    routeArrayList.removeLast()
                    k++
                }
            }
            val min = candidates.minByOrNull { it.value }
            if (min == null) {
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

    /**
     * GUI: メインメニュー/駅情報
     */

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

    /**
     * GUI: メインメニュー/駅情報
     */

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
        val aqua = ChatColor.AQUA
        val bold = ChatColor.BOLD
        val darkGray = ChatColor.DARK_GRAY
        val gray = ChatColor.GRAY
        val reset = ChatColor.RESET
        val white = ChatColor.WHITE
        sb.append("${gray}===== ${aqua}${bold}${station.name}${reset}${white}駅 ${gray}=====\n")
        sb.append("${gray}読み:${white}${station.yomi}\n")
        sb.append("${gray}駅番号:")
        if (station.number == null) sb.append("${darkGray}無し\n")
        else sb.append("${white}${station.number}\n")
        sb.append(
            "${gray}座標:[X:${white}${station.location[0]}${gray},Z:${white}${station.location[1]}${gray}]付近(ここから約${white}${
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
        sb.append("${gray}会社:${white}${companies.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("${gray}路線:${white}${lines.joinToString(separator = "、", transform = { it.value.name })}\n")
        sb.append("${gray}近隣自治体:")
        if (municipalities.isEmpty()) sb.append("${darkGray}無し\n")
        else sb.append("${white}${municipalities.joinToString(separator = "、", transform = { it.name })}\n")
        sb.append("${gray}隣の駅:\n")
        station.paths.forEach {
            sb.append(" ${it.toStringForGuide(data)}\n")
        }
        sb.append("=".repeat(20))
        player.sendMessage(sb.toString())
    }

    /**
     * GUI: メインメニュー/このアプリについて
     */

    private fun showAbout() {
        player.sendMessage(
            "Knit乗換案内\n" +
                    "製作者:Knit\n" +
                    "データベース更新日:${data.update}\n" +
                    "経路は機械的に算出されたものです。必ずしも最適な経路ではない可能性があります。情報を利用したことによる損害は負いかねます。"
        )
    }

}