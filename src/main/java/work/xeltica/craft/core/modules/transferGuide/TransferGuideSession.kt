package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.item.ItemModule
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
import java.util.UUID
import java.util.function.Consumer
import kotlin.math.abs

/**
 * プレイヤーがアプリを開いてから閉じるまでの一連の流れ(セッション)を表します。
 * @author Knit prg.
 */
class TransferGuideSession(val player: Player) {
    private val knit = "fef8dd2a-762a-463a-916f-f2e1ac62041b"
    private val data = TransferGuideData()
    private val gui = Gui.getInstance()
    private val logger = Bukkit.getLogger()
    private var startId: String? = null
    private var endId: String? = null
    private var infoId: String? = null
    private val favorites: ArrayList<String> = arrayListOf()

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
        userData?.getStringList("${player.world.name}.favorites")?.run {
            this.forEach {
                if (data.isStationInWorld(it, player.world.name)) {
                    favorites.add(it)
                }
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
            StationChoiceTarget.START -> {
                startId = newId
                saveUserData()
                openMainMenu()
                return
            }

            StationChoiceTarget.END -> {
                endId = newId
                saveUserData()
                openMainMenu()
                return
            }

            StationChoiceTarget.INFO -> {
                infoId = newId
                saveUserData()
                showStationData()
                return
            }

            StationChoiceTarget.FAVORITES_ADD -> {
                favorites.add(newId)
                saveUserData()
                openFavoritesMenu()
                return
            }

        }
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
            userData.conf.set("${uuid}.${player.world.name}.favorites", favorites)
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
        val items = arrayListOf(
            MenuItem("出発地点:$startStationName", { chooseStation(StationChoiceTarget.START) }, Material.LIME_BANNER),
            MenuItem("到着地点:$endStationName", { chooseStation(StationChoiceTarget.END) }, Material.RED_BANNER),
            MenuItem("出発地点と到着地点を入れ替える", { reverseChosenStations() }, Material.LEVER),
            MenuItem("計算開始", { showRoute() }, Material.COMMAND_BLOCK_MINECART),
            MenuItem("駅情報", { openStationInfoMenu() }, Material.CHEST_MINECART),
        )
        if (data.availableWorlds[player.world.name] == "main") {
            items.add(MenuItem("お気に入りの駅", { openFavoritesMenu() }, Material.OAK_SIGN))
        }
        items.add(MenuItem("このアプリについて", { showAbout() }, Material.ENCHANTED_BOOK))
        items.add(MenuItem("終了", null, Material.BARRIER))
        if (player.isOp || player.uniqueId.toString() == knit) {
            val head = Bukkit.getPlayer(UUID.fromString(knit))?.run {
                ItemModule.getPlayerHead(this)
            }
            items.addAll(
                arrayListOf(
                    MenuItem("デバッグメニュー", { openDebugMenu() }, head ?: ItemStack(Material.NETHERITE_AXE), isShiny = true)
                )
            )
        }
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
            MenuItem("ランダム", { chooseStationRandom(stationChoiceTarget) }, Material.TARGET)
        )
        if (stationChoiceTarget != StationChoiceTarget.FAVORITES_ADD) {
            items.add(MenuItem("お気に入りの駅", { chooseStationFavorites(stationChoiceTarget) }, Material.OAK_SIGN))
        }
        val goToMenu: Consumer<MenuItem> = when (stationChoiceTarget) {
            StationChoiceTarget.START, StationChoiceTarget.END -> Consumer { openMainMenu() }
            StationChoiceTarget.INFO -> Consumer { openStationInfoMenu() }
            StationChoiceTarget.FAVORITES_ADD -> Consumer { openFavoritesMenu() }
        }
        items.add(MenuItem("戻る", goToMenu, Material.REDSTONE_TORCH))
        gui.openMenu(player, "駅選択", items)
    }

    /**
     * ランダムに駅を選択します。
     */
    private fun chooseStationRandom(stationChoiceTarget: StationChoiceTarget) {
        val stations = KStations.allStations(data).filterByWorld(player.world.name).filterByType("station").value
        val range = 0 until stations.size
        val station = stations[range.random()]
        setStationIdAndOpenMainMenu(station.id, stationChoiceTarget)
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
     * GUI: 駅選択/お気に入りの駅
     */
    private fun chooseStationFavorites(stationChoiceTarget: StationChoiceTarget) {
        val items = ArrayList<MenuItem>()
        favorites.forEach { stationId ->
            val station = data.stations[stationId] ?: return@forEach
            items.add(MenuItem(station.name ?: station.id, { setStationIdAndOpenMainMenu(stationId, stationChoiceTarget) }, Material.MINECART))
        }
        items.add(MenuItem("戻る", { chooseStation(stationChoiceTarget) }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "お気に入りの駅", items)
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
        val goToMenu: Consumer<MenuItem> = when (stationChoiceTarget) {
            StationChoiceTarget.START, StationChoiceTarget.END -> Consumer { openMainMenu() }
            StationChoiceTarget.INFO -> Consumer { openStationInfoMenu() }
            else -> Consumer { openMainMenu() }
        }
        items.add(MenuItem("戻る", goToMenu, Material.REDSTONE_TORCH))
        gui.openMenu(player, "駅一覧", items)
    }

    /**
     * GUI: 出発地点と到着地点を入れ替える
     */

    private fun reverseChosenStations() {
        val tempStart = startId
        val tempEnd = endId
        startId = tempEnd
        endId = tempStart
        saveUserData()
        openMainMenu()
    }

    /**
     * 選択した駅からルートを検索します。
     * A*アルゴリズムの実装になっているハズ、多分
     */
    private fun calcRoute(startId: String?, endId: String?, reverse: Boolean = false): KRoute? {
        if (startId == null && endId == null) {
            gui.error(player, "駅が設定されていません！")
            return null
        } else if (startId == endId) {
            gui.error(player, "出発地点と到着地点が同一です。")
            return null
        } else if (startId == null) {
            gui.error(player, "出発地点が設定されていません！")
            return null
        } else if (endId == null) {
            gui.error(player, "到着地点が設定されていません！")
            return null
        }
        val start = data.stations[startId]
        if (start == null) {
            gui.error(player, "出発地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${startId}")
            return null
        }
        val end = data.stations[endId]
        if (end == null) {
            gui.error(player, "到着地点に存在しない駅が指定されました。")
            logger.warning("[TransferGuideData] 存在しない駅ID:${endId}")
            return null
        }
        /**未探索の駅*/
        val unsearched = data.stations.toMutableMap()

        /**次の探索の候補となる駅(OPENリスト)*/
        val opened: MutableMap<KStation, OpenedValue> = mutableMapOf()

        /**探索済みの駅(CLOSEリスト)*/
        val closed: MutableSet<KStation> = mutableSetOf()

        /**終点までの駅数リスト*/
        val stepsList = calcStepsAndTimeTo(endId)
        if (data.consoleDebug) {
            logger.info("[TransferGuide(debug)] $stepsList")
        }
        //始点の駅を未探索から探索候補に移す
        opened[start] = OpenedValue(0, 0)
        unsearched.remove(startId)
        var i = 0
        val toRemove = arrayListOf<String>()
        unsearched.forEach {
            if (it.value.world != start.world) {
                toRemove.add(it.key)
            }
        }
        toRemove.forEach {
            unsearched.remove(it)
        }
        /**デバッグモードがオンの場合やエラー発生時に出力する文字列を生成するメソッド*/
        fun loopADebugString(): String {
            return "step=A${i}\n" +
                    "route=${startId}->${endId}\n" +
                    "unsearched=${unsearched.toList().joinToString { it.first }}\n" +
                    "opened=${opened.toList().joinToString { "${it.first.id}(${it.second.elapsedTime}:${it.second.priority})" }}\n" +
                    "closed=${closed.joinToString { it.id }}"
        }
        knitA@ while (i < data.loopMax) {
            //探索候補の駅に対し、それぞれの優先度を決定する
            //計算式:(((終点との直線距離÷2)+推定所要時間)×残り駅数),終点と同じ路線なら÷1.25
            opened.forEach {
                val xDistance = abs(end.location[0] - it.key.location[0]).toInt()
                val yDistance = abs(end.location[1] - it.key.location[1]).toInt()
                val priority = (xDistance + yDistance) / 8
                opened[it.key] = OpenedValue(it.value.elapsedTime, it.value.elapsedTime + priority)
            }
            if (data.consoleDebug) {
                logger.info("[TransferGuide(debug)] ${loopADebugString()}")
            }
            //探索候補の駅の内、優先度値が最も低いものを選択する
            val minOpenedStation = opened.minByOrNull { it.value.priority }
            //優先度値が最も低いものが見つからない場合、両駅間の経路は存在しないものと考えられる
            if (minOpenedStation == null) {
                gui.error(player, "指定された駅同士を結ぶ有効な経路が存在しません。")
                logger.warning("[TransferGuide] 最小値からの駅探索失敗A\n${loopADebugString()}")
                return null
            }
            //優先度値が最も低い駅を探索候補から探索済へ移動
            closed.add(minOpenedStation.key)
            opened.remove(minOpenedStation.key)
            //優先度値が最も低い駅の隣の駅を探索候補へ移動
            for (path in minOpenedStation.key.paths) {
                //快速線で並行する緩行線がある線は無視
                if (data.lines[path.line]?.rapid == true && path.rapidNotInParallel == false) continue
                val pathToInUnsearched = unsearched[path.to] ?: continue
                opened[pathToInUnsearched] = OpenedValue(minOpenedStation.value.elapsedTime + (path.time ?: 0), 0)
                unsearched.remove(pathToInUnsearched.id)
                //もし終点が含まれていれば、そこで終了
                if (endId == pathToInUnsearched.id) {
                    closed.add(pathToInUnsearched)
                    if (data.consoleDebug) {
                        logger.info("[TransferGuide(debug)] A_END\n${loopADebugString()}")
                    }
                    break@knitA
                }
            }
            i++
        }
        if (i >= data.loopMax && closed.none { it.id == endId }) {
            player.sendMessage("計算回数が既定の回数以上になった為、強制終了しました。")
            logger.warning("[TransferGuide] 計算回数(${i})がloopMax(${data.loopMax})以上になりました。\nloopMaxの値を上げても解決しない場合、バグの可能性があります。\n${loopADebugString()}")
            return null
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
                if (station.paths.any {
                        it.to == routeArrayList.last().id
                                && (it.line == "walk" || it.line == "boat" || data.lines[it.line]?.rapid == false || it.rapidNotInParallel == true)
                    }) {
                    candidates[station] = TransferGuideUtil.calcDistance(end.location, station.location)
                }
            }
            if (data.consoleDebug) {
                logger.info("[TransferGuideData(debug)] candidates=${candidates.toList().joinToString { it.first.id + "(" + it.second + ")" }}")
            }
            //駅が取得できない場合、袋小路に入ったものと考え、分岐点まで戻る
            if (candidates.isEmpty()) {
                var k = 0
                while (k < data.loopMax) {
                    routeArrayList.removeLast()
                    if (routeArrayList.isEmpty()) {
                        gui.error(player, "最小値からの駅探索Bに失敗しました。")
                        logger.warning("[TransferGuideData] 最小値からの駅探索失敗B\n${loopBDebugString()}")
                        return null
                    }
                    if (data.consoleDebug) {
                        logger.info("[TransferGuideData(debug)] step=Ba${k}\n${loopBDebugString()}")
                    }
                    for (path in routeArrayList.last().paths) {
                        if ((path.line == "walk" || path.line == "boat" || data.lines[path.line]?.rapid == false || path.rapidNotInParallel == true) || closed.any {
                                it.id == path.to
                            }) {
                            j++
                            continue@knitB
                        }
                    }
                    k++
                }
                j++
            }
            //取得した駅の内、最も経路リストの終端にある駅から最も遠い駅を選択(その方が駅数が少なくなると考えられる)
            val max = candidates.maxByOrNull { it.value }
            if (max == null) {
                gui.error(player, "最小値からの駅探索Bに失敗しました。")
                logger.warning("[TransferGuideData] 最小値からの駅探索失敗B\n${loopBDebugString()}")
                return null
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
            player.sendMessage("計算回数が規定の回数以上になった為、計算を打ち切りました。")
            logger.warning("[TransferGuide] 計算回数(${j})がloopMax(${data.loopMax})以上になりました。\nloopMaxの値を上げても解決しない場合、バグの可能性があります。\n${loopBDebugString()}")
            return null
        }
        //終点→始点になっているので、反転させる
        routeArrayList.reverse()
        val toBePassed = mutableSetOf<Int>()
        //快速線が利用可能な場合、飛ばせる駅を飛ばす
        routeArrayList.forEachIndexed { index, station ->
            if (toBePassed.contains(index)) return@forEachIndexed
            station.paths.forEach { path ->
                val otherStationIndex = routeArrayList.indexOf(routeArrayList.findLast { it.id == path.to })
                if (otherStationIndex > index) {
                    var indexChecking = index + 1
                    while (indexChecking < otherStationIndex) {
                        toBePassed.add(indexChecking)
                        indexChecking++
                    }
                }
            }
        }
        val routeArrayListTemp = arrayListOf<KStation>()
        routeArrayList.forEachIndexed { index, value ->
            if (!toBePassed.contains(index)) {
                routeArrayListTemp.add(value)
            }
        }
        if (reverse) {
            routeArrayListTemp.reverse()
        }
        val routeArray = routeArrayListTemp.toTypedArray()
        if (data.consoleDebug) {
            logger.info("routeArray=${routeArray.joinToString { it.id }}")
        }
        return KRoute(data, routeArray)
    }

    /**
     * ルートを表示します。
     */
    private fun showRoute() {
        val startToEnd = calcRoute(startId, endId)
        val endToStart: KRoute?
        var shorter: KRoute?
        try {
            endToStart = calcRoute(endId, startId, true)
            shorter = listOf(startToEnd, endToStart).minByOrNull { it?.getTime() ?: 0 }
        } catch (_: Exception) {
            shorter = startToEnd
        }
        player.sendMessage(shorter?.toStringForGuide() ?: "失敗")
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
     * GUI: お気に入りの駅
     */
    private fun openFavoritesMenu() {
        val items = arrayListOf(
            MenuItem("追加", { chooseStation(StationChoiceTarget.FAVORITES_ADD) }, Material.BLACK_DYE),
            MenuItem("削除", { deleteFavoritesMenu() }, Material.WHITE_DYE),
            MenuItem("並び替え", { sortFavoritesMenu(null) }, Material.CHEST),
            MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH),
        )
        gui.openMenu(player, "お気に入りの駅", items)
    }

    /**
     * GUI: お気に入りの駅/削除
     */
    private fun deleteFavoritesMenu() {
        val items = ArrayList<MenuItem>()
        favorites.forEach { station ->
            items.add(MenuItem(data.stations[station]?.name ?: station, { deleteFavoritesAndOpenDeleteFavoritesMenu(station) }, Material.MINECART))
        }
        items.add(MenuItem("戻る", { openFavoritesMenu() }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "お気に入りの駅の削除", items)
    }

    /**
     * お気に入りの駅を消して削除メニューに戻る
     */
    private fun deleteFavoritesAndOpenDeleteFavoritesMenu(station: String) {
        favorites.remove(station)
        saveUserData()
        deleteFavoritesMenu()
    }

    /**
     * GUI: お気に入りの駅/並び替え
     */
    private fun sortFavoritesMenu(first: String?) {
        val items = ArrayList<MenuItem>()
        favorites.forEach { station ->
            val nextAction: Consumer<MenuItem> = when (first) {
                null -> {
                    Consumer { sortFavoritesMenu(station) }
                }

                station -> {
                    Consumer { sortFavoritesMenu(null) }
                }

                else -> {
                    Consumer { sortFavoritesAndOpenFavoritesMenu(first, station) }
                }
            }
            val isShiny = first == station
            items.add(MenuItem(data.stations[station]?.name ?: station, nextAction, Material.MINECART, shiny = isShiny))
        }
        items.add(MenuItem("戻る", { openFavoritesMenu() }, Material.REDSTONE_TORCH))
        gui.openMenu(player, "並び替え", items)
    }

    /**
     * お気に入りの駅を入れ替えてお気に入りメニューに戻る
     */
    private fun sortFavoritesAndOpenFavoritesMenu(first: String, second: String) {
        val firstIndex = favorites.indexOf(first)
        val secondIndex = favorites.indexOf(second)
        favorites[firstIndex] = second
        favorites[secondIndex] = first
        sortFavoritesMenu(null)
    }

    /**
     * GUI: メインメニュー/このアプリについて
     */
    private fun showAbout() {
        player.sendMessage(
            "${ChatColor.BOLD}乗換案内${ChatColor.RESET}\n" +
                    "経路は機械的に算出されたものです。必ずしも最適な経路ではない可能性があります。\n" +
                    "情報を利用したことによる損害は負いかねます。\n" +
                    "データベース更新日:${data.update}\n" +
                    "更新内容:\n" +
                    data.updateInfo
        )
    }

    /**
     * GUI: メインメニュー/デバッグメニュー
     */
    private fun openDebugMenu() {
        val items = arrayListOf(
            MenuItem("路線データの整合性チェック", { verifyData() }, Material.SPYGLASS),
            MenuItem("全経路チェック", { verifyAllRoutesWarn(false) }, Material.SPYGLASS),
            MenuItem("全経路チェック(到達可能性のみ)", { verifyAllRoutesWarn(true) }, Material.SPYGLASS),
            MenuItem("戻る", { openMainMenu() }, Material.REDSTONE_TORCH),
        )
        gui.openMenu(player, "デバッグメニュー", items)
    }

    /**
     * 路線データの整合性をチェックします。
     */
    private fun verifyData() {
        val yellow = ChatColor.YELLOW
        var count = 0
        data.stations.forEach { station ->
            if (station.value.name == null) {
                player.sendMessage("${yellow}駅の名前の欠如:stations.${station.key}.name")
                count++
            }
            if (station.value.yomi == null) {
                player.sendMessage("${yellow}駅の読みの欠如:stations.${station.key}.yomi")
                count++
            }
            if (!data.availableWorlds.keys.contains(station.value.world)) {
                player.sendMessage("${yellow}非対応のワールドに存在する駅:stations.${station.key}.world")
                count++
            }
            if (station.value.location.size != 2) {
                player.sendMessage("${yellow}不正な座標データ:stations.${station.key}.location")
                count++
            }
            if (station.value.type == null) {
                player.sendMessage("${yellow}駅の種類の欠如:stations.${station.key}.type")
                count++
            }
            station.value.paths.forEachIndexed { i, path ->
                if (path.to == null) {
                    player.sendMessage("${yellow}行き先の存在しないpath:stations.${station.key}.paths[$i].to")
                    count++
                } else if (!data.stationExists(path.to)) {
                    player.sendMessage("${yellow}存在しない駅ID:${path.to}(stations.${station.key}.paths[$i].to)")
                    count++
                } else if (path.to == station.key) {
                    player.sendMessage("${yellow}自駅への参照:stations.${station.key}.paths[$i].to")
                    count++
                }
                if (path.line == null) {
                    player.sendMessage("${yellow}路線が指定されていないpath:stations.${station.key}.paths[$i].line")
                    count++
                } else if ((path.line != "walk" && path.line != "boat") && !data.lineExists(path.line)) {
                    player.sendMessage("${yellow}存在しない路線ID:${path.line}(stations.${station.key}.paths[$i].line)")
                    count++
                }
                if (path.direction == null) {
                    player.sendMessage("${yellow}方向が指定されていないpath:stations.${station.key}.paths[$i].direction")
                    count++
                } else if (!data.directionExists(path.direction)) {
                    player.sendMessage("${yellow}存在しない方向ID:${path.direction}(stations.${station.key}.paths[$i].direction)")
                    count++
                }
                if (path.time == null) {
                    player.sendMessage("${yellow}時間が指定されていないpath:stations.${station.key}.paths[$i].time")
                    count++
                } else if (path.time <= 0) {
                    player.sendMessage("${yellow}無効な所要時間:${path.time}(stations.${station.key}.paths[$i].time)")
                    count++
                }
                if (path.rapidNotInParallel == null) {
                    player.sendMessage("${yellow}非並行緩行線の有無が未指定:stations.${station.key}.paths[$i].rapid_not_in_parallel")
                    count++
                }
            }
        }
        val unlinkedStations = data.stations.toMutableMap()
        data.lines.forEach { line ->
            line.value.stations.forEachIndexed { i, station ->
                unlinkedStations.remove(station)
                if (!data.stationExists(station)) {
                    player.sendMessage("${yellow}存在しない駅ID:${station}(lines.${line.key}.stations[$i])")
                    count++
                }
            }
        }
        unlinkedStations.forEach { station ->
            if (station.value.paths.all { it.line != "walk" && it.line != "boat" }) {
                player.sendMessage("${yellow}どの路線にも属していない駅:stations.${station.key}")
                count++
            }
        }
        data.companies.forEach { company ->
            company.value.lines.forEachIndexed { i, line ->
                if (!data.lineExists(line)) {
                    player.sendMessage("${yellow}存在しない路線ID:${line}(companies.${company.key}.lines[$i])")
                    count++
                }
            }
        }
        data.municipalities.forEach { municipality ->
            municipality.value.stations.forEachIndexed { i, station ->
                if (!data.stationExists(station)) {
                    player.sendMessage("${yellow}存在しない駅ID:${station}(municipalities.${municipality.key}.stations[$i])")
                    count++
                }
            }
        }
        if (count == 0) {
            player.sendMessage("データに誤りは見つかりませんでした。")
        } else {
            player.sendMessage("${yellow}${count}個のデータ誤りが見つかりました。")
        }
    }

    /**
     * 全駅が全駅に対して到達可能であることを確認する前の確認をします。
     */
    private fun verifyAllRoutesWarn(onlyReachable: Boolean) {
        gui.openTextInput(
            player, "この処理にはサーバーのパフォーマンスに影響を与える可能性があります。本当に実行しますか？\n" +
                    "実行する場合、「Knitは天才」、実行しない場合はそれ以外の文字列をチャット欄に打ち込んで下さい。\n" +
                    "${ChatColor.YELLOW}あなたがスタッフではない場合、セキュリティ上重大なバグが発生しています。実行せずにバグ報告をして下さい。"
        ) {
            if (!data.allowsElephant || it != "Knitは天才" || !player.isOp) {
                player.sendMessage("実行をキャンセルしました。")
                return@openTextInput
            }
            val thread = object : Thread() {
                override fun run() {
                    verifyAllRoutes(onlyReachable)
                }
            }
            thread.start()
        }
    }

    /**
     * 全駅が全駅に対して到達可能であることを確認します。
     */
    private fun verifyAllRoutes(onlyReachable: Boolean) {
        var problemCount = 0
        var itemCount = 0
        val yellow = ChatColor.YELLOW
        val stations = KStations.allStations(data)
            .filterByWorld(player.world.name)
            .filterByType("station")
            .value
        val total = stations.size * stations.size
        stations.forEach { start ->
            stations.forEach inner@{ end ->
                if (start.id == end.id) {
                    itemCount++
                    return@inner
                }
                try {
                    val startToEnd = calcRoute(start.id, end.id)
                    if (startToEnd == null) {
                        player.sendMessage("${yellow}${start.id}->${end.id}:到達不可。")
                        problemCount++
                    }
                    if (!onlyReachable) {
                        val endToStart = calcRoute(end.id, start.id)
                        if (endToStart == null) {
                            player.sendMessage("${yellow}${end.id}から${start.id}へ到達できません。")
                            problemCount++
                        }
                        val startToEndTime = startToEnd?.getTime()
                        val endToStartTime = endToStart?.getTime()
                        val difference = abs((startToEndTime ?: 0) - (endToStartTime ?: 0))
                        if (difference >= 30) {
                            player.sendMessage("${yellow}${start.id}<->${end.id}:行きと帰りで${TransferGuideUtil.secondsToString(difference)}の差があります")
                            problemCount++
                        }
                    }
                } catch (e: Exception) {
                    player.sendMessage("${yellow}${start.id}<->${end.id}:例外発生:コンソール参照")
                    e.printStackTrace()
                    problemCount++
                }
                itemCount++
                if (itemCount % 100 == 0) {
                    player.sendMessage("検証中[${itemCount}/${total}]")
                }
            }
        }
        player.sendMessage("検証終了[${itemCount}/${total}]")
        if (problemCount == 0) {
            player.sendMessage("全ての駅からすべての駅に到達可能です。")
        } else {
            player.sendMessage("${yellow}${problemCount}個の問題が見つかりました。")
        }
    }

    /**
     * 特定の駅へのステップ数と所要時間を計算します。
     */
    private fun calcStepsAndTimeTo(destination: String): Map<String, Pair<Int, Int>> {
        val map = mutableMapOf<String, Pair<Int, Int>>()
        val stations = data.stations.toMutableMap()
        map[destination] = Pair(0, 0)
        stations.remove(destination)
        var i = 1
        while (i < data.loopMax && stations.isNotEmpty()) {
            val beforeStepStations = map.filter { it.value.first == i - 1 }
            beforeStepStations.forEach { beforeStepStation ->
                data.stations[beforeStepStation.key]?.paths?.forEach { path ->
                    val pathTo = stations[path.to]
                    if (pathTo != null) {
                        map[pathTo.id] = Pair(i, beforeStepStation.value.second + (path.time ?: 0))
                        stations.remove(path.to)
                    }
                }
            }
            i++
        }
        return map
    }

}

private class OpenedValue(
    var elapsedTime: Int,
    var priority: Int,
)