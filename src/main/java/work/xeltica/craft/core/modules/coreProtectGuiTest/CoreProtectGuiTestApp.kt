package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリ
 */
class CoreProtectGuiTestApp : AppBase() {

    /**
     * インスタンスを使いまわすやつ
     */
    private val gui by lazy { Gui.getInstance() }
    private val coreProtectCommand by lazy { CoreProtectCommand() }

    /**
     * 範囲を指定するときにつかう
     */
    private lateinit var firstDateTime: Pair<Int, String>
    private lateinit var secondDateTime: Pair<Int, String>

    private var commandFirst: String = "co"

    override fun getName(player: Player): String {
        return "CoreProtectGUIテストアプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.ENDER_EYE
    }

    override fun onLaunch(player: Player) {
        showMenu(player, "選択する方法を選択してください", coreProtectModeList(player))
    }

    /**
     * 渡されたアイテムからメニュー画面を開く
     *
     * @param player メニューを開くプレイヤー
     * @param title 開くメニューのタイトル
     * @param items 開くメニューのアイテム一覧
     * @return メニューアイテムのリスト
     */
    private fun showMenu(player: Player, title: String, items: List<MenuItem>) {
        gui.openMenu(player, title, items)
    }

    /**
     * 1度目に取得した日時と2度目に取得した日時を比較して
     * [CoreProtectCommand.date]に格納する文字列を決定するやつ
     */
    private fun checkDuringTime() {
        val firstDate = firstDateTime.first.toLong() * convertUnit(firstDateTime.second)
        val lastDate = secondDateTime.first.toLong() * convertUnit(secondDateTime.second)

        if (firstDate < lastDate) {
            coreProtectCommand.date = (firstDateTime.first.toString() + firstDateTime.second) + "-" + (secondDateTime.first.toString() + secondDateTime.second)
            Bukkit.getLogger().info("FirstDate more than")
        } else if (firstDate > lastDate) {
            coreProtectCommand.date = (secondDateTime.first.toString() + secondDateTime.second) + "-" + (firstDateTime.first.toString() + firstDateTime.second)
            Bukkit.getLogger().info("LastDate more than")
        } else {
            // ここに到達する場合は同値である場合なので、onTimeUnitMenuClick と同じ経路になるような形で
            // コマンドの文字列を確定させる
            coreProtectCommand.date = firstDateTime.first.toString() + firstDateTime.second
        }
        Bukkit.getLogger().info(coreProtectCommand.date)
    }

    /**
     * 単位を秒数に変換する
     *
     * @param unit 単位
     * @return 単位を秒数に変換した数値。値がでかくなるので[Long]で変換
     *
     * NOTE: 合理的な処理が思いつかなかった
     */
    private fun convertUnit(unit: String): Long {
        return when (unit) {
            "s" -> {
                1
            }

            "m" -> {
                60
            }

            "h" -> {
                3600
            }

            "d" -> {
                86400
            }

            "w" -> {
                604800
            }

            else -> {
                0
            }
        }
    }

    /**
     * コマンドをコンソールに送る
     *
     * @param player コマンドを送るプレイヤー
     */
    private fun commandSend(player: Player) {
        val sendCommand = StringBuilder()
        sendCommand.append(commandFirst)

        // あくまで空文字だったらコマンドのパラメーターから除外するって処理なので、ここら辺はうまい具合にできそうかも
        if (coreProtectCommand.user.isNotBlank()) {
            sendCommand.append("user:" + coreProtectCommand.user + " ")
        }
        if (coreProtectCommand.radius.isNotBlank()) {
            sendCommand.append("radius:" + coreProtectCommand.radius + " ")
        }
        if (coreProtectCommand.date.isNotBlank()) {
            sendCommand.append("time:" + coreProtectCommand.date + " ")
        }
        if (coreProtectCommand.action.isNotBlank()) {
            sendCommand.append("action:" + coreProtectCommand.action + " ")
        }

        Bukkit.getLogger().info(sendCommand.toString())
        player.performCommand(sendCommand.toString())
    }

    private fun commandSend(value: Int, player: Player) {
        player.performCommand("coreprotect:co lookup $value")
    }

    /**
     * インスペクトモードの切り替えが選択されたら呼ばれる
     *
     * @param player メニューを選択したプレイヤー
     */
    private fun onInspectMode(player: Player) {
        player.performCommand("co i")
    }

    /**
     * プレイヤー名を入力したら呼ばれる
     *
     * @param userName 入力したプレイヤー名
     * @param player メニューを開いたプレイヤー
     */
    private fun onInputPlayerName(userName: String, player: Player) {
        coreProtectCommand.user = userName
        val radiusWorldList = getRadiusList(player)

        showMenu(player, "ワールドの範囲を指定してください", radiusWorldList)
    }

    /**
     * ワールド範囲を選択したら呼ばれる
     *
     * @param radius ログを取るワールドの範囲
     * @param player メニューを開いたプレイヤー
     */
    private fun onRadiusWorldMenuClick(radius: String, player: Player) {
        coreProtectCommand.radius = radius

        val duringModeList = getDuringModeList(player)
        showMenu(player, "時間の指定方法を選択してください", duringModeList)
    }

    /**
     * 期間指定かどうかを選択したら呼ばれる
     *
     * @param duringModeFlag 選択結果
     * @param player メニューを開いたプレイヤー
     */
    private fun onSelectDuringMode(duringModeFlag: Boolean, player: Player) {
        if (duringModeFlag) {
            inputDuringTime(true, player)
        } else {
            inputTime(player)
        }
    }

    /**
     * 時間の単位を選択したら呼ばれる(伝播されてくる)
     *
     * @param dateTime 時間と単位をPairでまとめたやつ
     * @param flag 1個目の日時選択の場合はtrue
     * @param player メニューを開いたプレイヤー
     */
    private fun onDuringTimeUnit(player: Player, dateTime: Pair<Int, String>, flag: Boolean) {
        if (flag) {
            firstDateTime = dateTime
            inputDuringTime(false, player)
        } else {
            secondDateTime = dateTime
            checkDuringTime()
            val actionMenuList = getActionMenuList(player)
            showMenu(player, "アクションの種類を選択してください", actionMenuList)
        }
    }

    /**
     * 時間の単位を選択したら呼ばれる
     *
     * @param value 数値
     * @param unit  メニューで選択した時間の単位
     * @param player メニューを開いたプレイヤー
     */
    private fun onTimeUnitMenuClick(value: Int, unit: String, player: Player) {
        coreProtectCommand.date = value.toString() + unit
        val actionMenuList = getActionMenuList(player)
        showMenu(player, "アクションの種類を選択してください", actionMenuList)
    }

    /**
     * アクションを指定しなかった場合に呼ばれる
     *
     * @param player メニューを選択したプレイヤー
     */
    private fun onNoDesignate(player: Player) {
        commandSend(player)
    }

    /**
     * action の種類を選択したら呼ばれる
     *
     * @param action メニューから指定したアクション
     * @param player メニューを開いたプレイヤー
     */
    private fun onActionMenuClick(action: String, player: Player) {
        Bukkit.getLogger().info(action)
        val optionMenuList = getOptionMenuList(player, action)

        showMenu(player, "オプションを選択してください", optionMenuList)
    }

    /**
     * actionのオプションを選択したら呼ばれる
     *
     * @param action 最終的にコマンドに付与するアクションの形
     * @param player メニューを開いたプレイヤー
     */
    private fun onOptionMenuClick(action: String, player: Player) {
        coreProtectCommand.action = action
        commandSend(player)
    }

    /**
     * 処理を中断するために作ったやつ
     *
     * @param player メニューを開いたプレイヤー
     */
    private fun onCancel(player: Player) {
        player.sendMessage("処理をキャンセルします")
    }

    /**
     * coreProtectの処理で何かをするときに呼ばれる
     *
     * @param player 何かを開始するプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun coreProtectModeList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("ログを検索する", { lookupModeStart(player) }, Material.WRITABLE_BOOK),
                MenuItem("ロールバック", { rollbackModeStart(player) }, Material.ENDER_CHEST),
                MenuItem("ページを取得する", { inputLookUpPage(player) }, Material.BOOK),
                MenuItem("インスペクトモード切り替え", { onInspectMode(player) }, Material.ENDER_EYE),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * ログを検索する
     *
     * @param player ログを取りにいくプレイヤー
     */
    private fun lookupModeStart(player: Player) {
        commandFirst = "co lookup "
        showMenu(player, "ログを取るプレイヤーを指定", chooseHowToGetPlayerName(player))
    }

    /**
     * ロールバックを実行する
     *
     * @param player ロールバックを実行するプレイヤー
     */
    private fun rollbackModeStart(player: Player) {
        commandFirst = "co rollback "
        showMenu(player, "どのプレイヤーの行動をロールバックするのかを指定", chooseHowToGetPlayerName(player))
    }

    /**
     * ページ番号を入力させる
     *
     * @param player コマンドを打ちたいプレイヤー
     */
    private fun inputLookUpPage(player: Player) {
        gui.openTextInput(player, "閲覧するページを選択してください") { inputString ->
            val value = inputString.toIntOrNull()
            value?.let {
                if (it <= 0) {
                    Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                    return@openTextInput
                }

                commandSend(value, player)
            } ?: run {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }
        }
    }

    /**
     * メニューからログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     */
    private fun getChooseByPlayerMenu(player: Player) {
        gui.openPlayersMenu(player, "プレイヤーを指定してください", { playerName ->
            playerName.player?.name?.let {
                onInputPlayerName(it, player)
            }
        }, { it.uniqueId != player.uniqueId || it.uniqueId == player.uniqueId })
        return
    }

    /**
     * プレイヤーネームの取得方法を選択するメニューアイテムのリストを返す
     *
     * @param [player] メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun chooseHowToGetPlayerName(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("コマンドで取得する", { getInputTextByConsole(player) }, Material.WRITABLE_BOOK),
                MenuItem("現在いるプレイヤーから取得する", { getChooseByPlayerMenu(player) }, Material.BOOK),
                MenuItem("クリーパー", { onInputPlayerName("#creeper", player) }, Material.CREEPER_HEAD),
                MenuItem("TNT", { onInputPlayerName("#tnt", player) }, Material.TNT),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * テキストでログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     */
    private fun getInputTextByConsole(player: Player) {
        gui.openTextInput(player, "プレイヤーのIDを指定してください") { userName ->
            onInputPlayerName(userName, player)
        }
        return
    }

    /**
     * ワールドの範囲を選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getRadiusList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("すべてのワールド", { onRadiusWorldMenuClick("#global", player) }, Material.DIRT),
                MenuItem("メインワールド", { onRadiusWorldMenuClick("#main", player) }, Material.CRAFTING_TABLE),
                MenuItem("共有ワールド", { onRadiusWorldMenuClick("#wildarea2", player) }, Material.GRASS_BLOCK),
                MenuItem("共有ネザー", { onRadiusWorldMenuClick("#wildarea2_nether", player) }, Material.NETHERRACK),
                MenuItem("共有エンド", { onRadiusWorldMenuClick("#wildarea2_the_end", player) }, Material.END_STONE),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * ～前までの取得か期間での取得かを選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getDuringModeList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("とある日時まで遡る", { onSelectDuringMode(false, player) }, Material.CLOCK),
                MenuItem("期間を指定して取得する", { onSelectDuringMode(true, player) }, Material.CLOCK),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * time の値を入力する
     *
     * @param player メニューを開くプレイヤー
     */
    private fun inputTime(player: Player) {
        gui.openTextInput(player, "時間の数値(整数)を入力してください。") { inputString ->
            val value = inputString.toIntOrNull()
            value?.let {
                if (it <= 0) {
                    Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                    return@openTextInput
                }

                showMenu(player, "時間の単位を選択してください", getTimeUnitList(value, player))
            } ?: run {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }
        }
    }

    /**
     * ログを取得する単位を選択するメニューリストを返す
     *
     * @param value 単位の前の数値
     * @param player メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getTimeUnitList(value: Int, player: Player): List<MenuItem> {
        return listOf(
                MenuItem("週", { onTimeUnitMenuClick(value, "w", player) }, Material.CLOCK),
                MenuItem("日", { onTimeUnitMenuClick(value, "d", player) }, Material.CLOCK),
                MenuItem("時", { onTimeUnitMenuClick(value, "h", player) }, Material.CLOCK),
                MenuItem("分", { onTimeUnitMenuClick(value, "m", player) }, Material.CLOCK),
                MenuItem("秒", { onTimeUnitMenuClick(value, "s", player) }, Material.CLOCK),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * time の値を入力する(範囲選択用)
     *
     * @param player 入力をするプレイヤー
     * @param flag 1個目の日時選択の場合はtrue
     */
    private fun inputDuringTime(flag: Boolean, player: Player) {
        gui.openTextInput(player, "時間の数値(整数)を入力してください。") { inputString ->
            val value = inputString.toIntOrNull()
            value?.let {
                if (it <= 0) {
                    Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                    return@openTextInput
                }

                val duringUnitList = getDuringUnitList(value, flag, player)
                showMenu(player, "時間の単位を選択してください", duringUnitList)
            } ?: run {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }
        }
    }

    /**
     * ログを取得する単位を選択するメニューリストを返す(範囲選択用)
     *
     * @param value 単位の前の数値
     * @param flag 1個目の日時選択の場合はtrue
     * @param player メニューを開いたプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getDuringUnitList(value: Int, flag: Boolean, player: Player): List<MenuItem> {
        return listOf(
                MenuItem("週", { onDuringTimeUnit(player, Pair(value, "w"), flag) }, Material.CLOCK),
                MenuItem("日", { onDuringTimeUnit(player, Pair(value, "d"), flag) }, Material.CLOCK),
                MenuItem("時", { onDuringTimeUnit(player, Pair(value, "h"), flag) }, Material.CLOCK),
                MenuItem("分", { onDuringTimeUnit(player, Pair(value, "m"), flag) }, Material.CLOCK),
                MenuItem("秒", { onDuringTimeUnit(player, Pair(value, "s"), flag) }, Material.CLOCK),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * アクションを選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getActionMenuList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("チェスト", { onActionMenuClick("container", player) }, Material.CHEST_MINECART),
                MenuItem("ブロック", { onActionMenuClick("block", player) }, Material.GRASS_BLOCK),
                MenuItem("アイテム", { onActionMenuClick("item", player) }, Material.WHEAT_SEEDS),
                MenuItem("チャット", { onActionMenuClick("chat", player) }, Material.JUKEBOX),
                MenuItem("クリック", { onActionMenuClick("click", player) }, Material.BOOK),
                MenuItem("コマンド", { onActionMenuClick("command", player) }, Material.PLAYER_HEAD),
                MenuItem("キル", { onActionMenuClick("kill", player) }, Material.ZOMBIE_HEAD),
                MenuItem("ユーザーネーム", { onActionMenuClick("username", player) }, Material.WRITABLE_BOOK),
                MenuItem("指定しない", { onNoDesignate(player) }, Material.REDSTONE_TORCH),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * アクションのオプションを選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @param action 選択したアクションの文字列
     * @return メニューアイテムのリスト
     */
    private fun getOptionMenuList(player: Player, action: String): List<MenuItem> {
        return when (action) {
            "container" -> {
                listOf(
                        MenuItem("アイテムを入れた", { onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                        MenuItem("アイテムをとった", { onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )

            }

            "block" -> {
                listOf(
                        MenuItem("ブロックを設置した", { onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                        MenuItem("ブロックを破壊した", { onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )

            }

            "item" -> {
                listOf(
                        MenuItem("アイテムの拾得", { onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                        MenuItem("アイテムのドロップ", { onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )

            }

            "chat",
            "click",
            "command",
            "kill",
            "username" -> {
                listOf(
                        MenuItem("実行", { onOptionMenuClick(action, player) }, Material.REDSTONE),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )

            }
            // ここに来る想定はないが、現状ここでescapeで戻ることしかできないため、仮の値を作っておく
            else -> {
                listOf(
                        MenuItem("指定しない", { onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }
        }
    }

    /**
     * メニューで選択した値やコンソールから入力された送られた値をまとめたクラス
     *
     * NOTE: 初期化のためにlateinit を外したが、ちょっといい感じにできるかもしれない
     */
    private class CoreProtectCommand {
        var user: String = ""
        var radius: String = ""
        var date: String = ""
        var action: String = ""
    }

}