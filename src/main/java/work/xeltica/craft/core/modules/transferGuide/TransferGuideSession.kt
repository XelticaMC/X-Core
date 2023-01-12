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
import java.util.function.Consumer

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
            if (data.isStationInWorld(this, player.world.name)) {
                startId = this
            }
        }
        userData?.getString("${player.world.name}.end")?.run {
            if (data.isStationInWorld(this, player.world.name)) {
                endId = this
            }
        }
        userData?.getString("${player.world.name}.info")?.run {
            if (data.isStationInWorld(this, player.world.name)) {
                infoId = this
            }
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
        val startStationName = data.stations[startId]?.name ?: "未設定"
        val endStationName = data.stations[endId]?.name ?: "未設定"
        val items = listOf(
            MenuItem("出発地点:$startStationName", { chooseStation(StationChoiceTarget.START) }, Material.LIME_BANNER),
            MenuItem("到着地点:$endStationName", { chooseStation(StationChoiceTarget.END) }, Material.RED_BANNER),
            MenuItem("計算開始", { calcRoute() }, Material.COMMAND_BLOCK_MINECART),
            MenuItem("駅情報", { openStationInfoMenu() }, Material.CHEST_MINECART),
            MenuItem("このアプリについて", { showAbout() }, Material.ENCHANTED_BOOK),
            MenuItem("終了", null, Material.BARRIER)
        )
        gui.openMenu(player, "乗換案内メインメニュー", items)
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
        val goToMenu: Consumer<MenuItem> = when (stationChoiceTarget) {
            StationChoiceTarget.START, StationChoiceTarget.END -> Consumer { openMainMenu() }
            StationChoiceTarget.INFO -> Consumer { openStationInfoMenu() }
        }
        items.add(MenuItem("戻る", goToMenu, Material.REDSTONE_TORCH))
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
                MenuItem(station.name ?: station.id, { setStationIdAndOpenMainMenu(station.id, stationChoiceTarget) }, Material.MINECART)
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
        data.companies.forEach { company ->
            items.add(MenuItem(company.value.name ?: company.key, { chooseStationLine(stationChoiceTarget, company.value) }, Material.SPRUCE_DOOR))
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
                MenuItem(line.name ?: line.id, { chooseStationLine(stationChoiceTarget, company, line) }, Material.RAIL)
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, company.name ?: company.id, items)
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
                MenuItem(station.name ?: station.id, { setStationIdAndOpenMainMenu(station.id, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { chooseStationLine(stationChoiceTarget, company) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, line.name ?: "", items)
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
                MenuItem(muni.name ?: muni.id, { chooseStationMuni(stationChoiceTarget, muni) }, Material.FILLED_MAP)
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
                MenuItem(station.name ?: station.id, { setStationIdAndOpenMainMenu(station.id, stationChoiceTarget) }, Material.MINECART)
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
            val distance = TransferGuideUtil.metersToString(TransferGuideUtil.calcDistance(playerLocation, station.location))
            items.add(
                MenuItem("${station.name}(約${distance})", { setStationIdAndOpenMainMenu(station.id, stationChoiceTarget) }, Material.MINECART)
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
                MenuItem(station.name ?: station.id, { setStationIdAndOpenMainMenu(station.id, stationChoiceTarget) }, Material.MINECART)
            )
        }
        items.add(MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "駅一覧", items)
    }

    /**
     * 選択した駅からルートを検索します。
     * A*アルゴリズムの実装になっているハズ、多分
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
        /**未探索の駅*/
        val unsearched = data.stations.toMutableMap()

        /**次の探索の候補となる駅(OPENリスト)*/
        val opened: MutableMap<KStation, Double> = mutableMapOf()

        /**探索済みの駅(CLOSEリスト)*/
        val closed: MutableSet<KStation> = mutableSetOf()

        /**始点からの駅数リスト*/
        val stepsList = TransferGuideUtil.calcStepsTo(data, startId ?: "")
        if (data.consoleDebug) {
            logger.info("[TransferGuide(debug)] $stepsList")
        }
        //始点と終点が同一路線に属する駅の場合、その路線に属さない駅を未探索の駅から削除
        val sameLines = TransferGuideUtil.getSameLines(data, startId!!, endId!!)
        if (sameLines.isNotEmpty()) {
            val toRemove = ArrayList<String>()
            unsearched.forEach { station ->
                if (data.lines[sameLines[0]]?.stations?.contains(station.key) == false) {
                    toRemove.add(station.key)
                }
            }
            toRemove.forEach {
                unsearched.remove(it)
            }
            if (data.consoleDebug) {
                logger.info("[TransferGuideData(debug)] 同一路線:${sameLines[0]}")
            }
        }
        //始点の駅を未探索から探索候補に移す
        opened[start] = 0.0
        unsearched.remove(startId)
        var i = 0

        /**デバッグモードがオンの場合やエラー発生時に出力する文字列を生成するメソッド*/
        fun loopADebugString(): String {
            return "step=A${i}\n" +
                    "route=${startId}->${endId}\n" +
                    "unsearched=${unsearched.toList().joinToString { it.first }}\n" +
                    "opened=${opened.toList().joinToString { "${it.first.id}(${it.second})" }}\n" +
                    "closed=${closed.joinToString { it.id }}"
        }
        knitA@ while (i < data.loopMax) {
            //探索候補の駅に対し、それぞれの優先度を決定する
            //計算式:(終点との直線距離×残り液数),同じ路線なら÷2
            opened.forEach {
                if (it.value == 0.0) {
                    var distance = TransferGuideUtil.calcDistance(end.location, it.key.location)
                    distance *= stepsList[it.key.id] ?: 1
                    if (TransferGuideUtil.getSameLines(data, it.key.id, endId!!).isNotEmpty()) {
                        distance /= 2
                    }
                    opened[it.key] = distance
                }
            }
            if (data.consoleDebug) {
                logger.info("[TransferGuide(debug)] ${loopADebugString()}")
            }
            //探索候補の駅の内、優先度値が最も低いものを選択する
            val minOpenedStation = opened.minByOrNull { it.value }
            //優先度値が最も低いものが見つからない場合、両駅間の経路は存在しないものと考えられる
            if (minOpenedStation == null) {
                gui.error(player, "指定された駅同士を結ぶ有効な経路が存在しません。")
                logger.warning("[TransferGuide] 最小値からの駅探索失敗A\n${loopADebugString()}")
                return
            }
            //優先度値が最も低い駅を探索候補から探索済へ移動
            closed.add(minOpenedStation.key)
            opened.remove(minOpenedStation.key)
            //優先度値が最も低い駅の隣の駅を探索候補へ移動
            for (path in minOpenedStation.key.paths) {
                val pathToInUnsearched = unsearched[path.to] ?: continue
                opened[pathToInUnsearched] = 0.0
                unsearched.remove(pathToInUnsearched.id)
                //もし終点が含まれていれば、そこで終了
                if (endId == pathToInUnsearched.id) {
                    closed.add(pathToInUnsearched)
                    if (data.consoleDebug) {
                        logger.info("[TransferGuide(debug)] A_END\n${loopADebugString()}")
                    }
                    break@knitA
                }
                //終着駅と同じ路線に辿り着いたら、その他の路線に行かないようにする
                val sameLinesFromMinStation = TransferGuideUtil.getSameLines(data, minOpenedStation.key.id, endId!!)
                if (sameLinesFromMinStation.isNotEmpty()) {
                    if (data.consoleDebug) {
                        logger.info("[TransferGuide(debug)] 同一路線:${sameLinesFromMinStation[0]}")
                    }
                    val toRemove = ArrayList<KStation>()
                    opened.forEach {
                        if (data.lines[sameLinesFromMinStation[0]]?.stations?.contains(it.key.id) == false) {
                            toRemove.add(it.key)
                        }
                    }
                    unsearched.forEach {
                        if (data.lines[sameLinesFromMinStation[0]]?.stations?.contains(it.key) == false) {
                            toRemove.add(it.value)
                        }
                    }
                    toRemove.forEach {
                        opened.remove(it)
                        unsearched.remove(it.id)
                    }
                }
            }
            i++
        }
        if (i >= data.loopMax && closed.none { it.id == endId }) {
            player.sendMessage("計算回数が既定の回数以上になった為、強制終了しました。")
            logger.warning("[TransferGuide] 計算回数(${i})がloopMax(${data.loopMax})以上になりました。\nloopMaxの値を上げても解決しない場合、バグの可能性があります。\n${loopADebugString()}")
            return
        }
        /**経路を通る駅の一覧で表したリスト*/
        val routeArrayList = ArrayList<KStation>()
        routeArrayList.add(end)
        closed.removeIf { it.id == endId }
        var j = 0

        /**デバッグモードがオンの場合やエラー発生時に出力する文字列を生成するメソッド*/
        fun loopBDebugString(): String {
            return "step=B${j}\n" +
                    "route=${startId}->${endId}\n" +
                    "closed=${closed.joinToString { it.id }}\n" +
                    "routeArrayList=${routeArrayList.joinToString { it.id }}"
        }
        //探索済みリストを遡ってルートを決定する
        knitB@ while (j < data.loopMax) {
            if (data.consoleDebug) {
                logger.info("[TransferGuideData(debug)] ${loopBDebugString()}")
            }
            //経路リストの終端にある駅から繋がっている駅の内、探索済みリストにある駅を全て取得
            val candidates: MutableMap<KStation, Double> = mutableMapOf()
            closed.forEach { station ->
                if (station.paths.any { it.to == routeArrayList.last().id }) {
                    candidates[station] = TransferGuideUtil.calcDistance(end.location, station.location)
                }
            }
            //駅が取得できない場合、袋小路に入ったものと考え、分岐点まで戻る
            if (candidates.isEmpty()) {
                var k = 0
                while (k < data.loopMax) {
                    if (data.consoleDebug) {
                        logger.info("[TransferGuideData(debug)] step=Ba${k}\n${loopBDebugString()}")
                    }
                    for (path in routeArrayList.last().paths) {
                        if (closed.any { it.id == path.to }) {
                            continue@knitB
                        }
                    }
                    routeArrayList.removeLast()
                    k++
                }
            }
            //取得した駅の内、最も経路リストの終端にある駅から最も遠い駅を選択(その方が駅数が少なくなると考えられる)
            val max = candidates.maxByOrNull { it.value }
            if (max == null) {
                gui.error(player, "最小値からの駅探索Bに失敗しました。")
                logger.warning("[TransferGuideData] 最小値からの駅探索失敗B\n${loopBDebugString()}")
                return
            }
            //選択した駅を探索済みリストから経路リストに移動
            routeArrayList.add(max.key)
            closed.remove(max.key)
            //始点に到達したら終了
            if (max.key.id == startId) {
                if (data.consoleDebug) {
                    logger.info("[TransferGuide] step: B_END\n${loopBDebugString()}")
                }
                break@knitB
            }
            j++
        }
        if (j >= data.loopMax) {
            player.sendMessage("計算回数が規定の回数以上になった為、計算を打ち切りました。以下の出力結果は正しくない場合があります。")
            logger.warning("[TransferGuide] 計算回数(${j})がloopMax(${data.loopMax})以上になりました。\nloopMaxの値を上げても解決しない場合、バグの可能性があります。\n${loopBDebugString()}")
        }
        //終点→始点になっているので、反転させる
        routeArrayList.reverse()
        val routeArray = routeArrayList.toTypedArray()
        //結果表示
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
                    if (!(companies.contains(company)) && company.lines.contains(it.key)) {
                        companies.add(company)
                    }
                }
            }
        val municipalities = data.municipalities.values.filter { it.stations.contains(infoId) }
        val bold = ChatColor.BOLD
        val darkGray = ChatColor.DARK_GRAY
        val gray = ChatColor.GRAY
        val reset = ChatColor.RESET
        val white = ChatColor.WHITE
        sb.append("${gray}===== ${white}${bold}${station.name}${reset}${white}駅 ${gray}=====\n")
        sb.append("${gray}読み:${white}${station.yomi}\n")
        sb.append("${gray}駅番号:")
        if (station.number == null) {
            sb.append("${darkGray}無し\n")
        } else {
            sb.append("${white}${station.number}\n")
        }
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
        sb.append("${gray}会社:${white}${companies.joinToString(separator = "、", transform = { it.name ?: "" })}\n")
        sb.append("${gray}路線:${white}${lines.joinToString(separator = "、", transform = { it.value.name ?: "" })}\n")
        sb.append("${gray}近隣自治体:")
        if (municipalities.isEmpty()) {
            sb.append("${darkGray}無し\n")
        } else {
            sb.append("${white}${municipalities.joinToString(separator = "、", transform = { it.name ?: "" })}\n")
        }
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