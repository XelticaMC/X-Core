package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリのモジュール
 */
object CoreProtectGuiTestModule : ModuleBase() {

    /**
     * [CoreProtectGuiTestApp]
     *
     * NOTE: アプリとモジュールでのやりとりがかなり多いのでインスタンス生成処理の削減のためにメンバ変数として宣言する
     */
    private val app by lazy { CoreProtectGuiTestApp() }
    private val gui by lazy { Gui.getInstance() }

    override fun onEnable() {
        // むかつくので消さない
        Bukkit.getLogger().info("モジュールをボンジュール～")
    }

    /**
     * テキストでログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     */
    private fun getInputTextByConsole(player: Player) {
        gui.openTextInput(player, "ログを取るプレイヤーを指定してください") { userName ->
            userName.let {
                app.onInputPlayerName(it, player)
            }
        }
        return
    }

    /**
     * メニューからログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     */
    private fun getChooseByPlayerMenu(player: Player) {
        gui.openPlayersMenu(player, "ログを取るプレイヤーを指定してください", { playerName ->
            playerName.player?.name?.let {
                app.onInputPlayerName(it, player)
            }
        }, { it.uniqueId != player.uniqueId || it.uniqueId == player.uniqueId })
        return
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
                MenuItem("ページを取得する", { inputLookUpPage(player) }, Material.BOOK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * ログを検索する
     *
     * @param player ログを取りにいくプレイヤー
     */
    private fun lookupModeStart(player: Player) {
        showMenu(player, "ログを取るプレイヤーを指定", chooseHowToGetPlayerName(player))
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

                app.commandSend(value, player)
            } ?: run {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }
        }
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
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
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
     * ログを取得する単位を選択するメニューリストを返す
     *
     * @param value 単位の前の数値
     * @param player メニューを開くプレイヤー
     * @return メニューアイテムのリスト
     */
    private fun getTimeUnitList(value: Int, player: Player): List<MenuItem> {
        return listOf(
                MenuItem("週", { app.onTimeUnitMenuClick(value, "w", player) }, Material.CLOCK),
                MenuItem("日", { app.onTimeUnitMenuClick(value, "d", player) }, Material.CLOCK),
                MenuItem("時", { app.onTimeUnitMenuClick(value, "h", player) }, Material.CLOCK),
                MenuItem("分", { app.onTimeUnitMenuClick(value, "m", player) }, Material.CLOCK),
                MenuItem("秒", { app.onTimeUnitMenuClick(value, "s", player) }, Material.CLOCK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
        )
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
                MenuItem("週", { onDuringMenuClick(value, "w", flag, player) }, Material.CLOCK),
                MenuItem("日", { onDuringMenuClick(value, "d", flag, player) }, Material.CLOCK),
                MenuItem("時", { onDuringMenuClick(value, "h", flag, player) }, Material.CLOCK),
                MenuItem("分", { onDuringMenuClick(value, "m", flag, player) }, Material.CLOCK),
                MenuItem("秒", { onDuringMenuClick(value, "s", flag, player) }, Material.CLOCK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * 時間の単位を選択したら呼ばれる(範囲選択用)
     *
     * @param value 単位の前の数値
     * @param unit  メニューで選択した時間の単位
     * @param flag 1個目の日時選択の場合はtrue
     * @param player プレイヤー
     */
    private fun onDuringMenuClick(value: Int, unit: String, flag: Boolean, player: Player) {
        app.onDuringTimeUnit(player, Pair(value, unit), flag)
    }


    /**
     * 範囲選択で日時を取得する場合に呼ばれる
     *
     * @param player 入力をするプレイヤー
     * @param flag 1個目の日時選択の場合はtrue
     */
    fun getTimeValueAndUnit(player: Player, flag: Boolean) {
        inputDuringTime(flag, player)
    }

    /**
     * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリの開始
     *
     * @param player コマンドを打つプレイヤー
     */
    fun start(player: Player) {
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
    fun showMenu(player: Player, title: String, items: List<MenuItem>) {
        gui.openMenu(player, title, items)
    }

    /**
     * time の値を入力する
     *
     * @param player メニューを開くプレイヤー
     */
    fun inputTime(player: Player) {
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

    class GetMenuList {
        /**
         * ワールドの範囲を選択するメニューリストを返す
         *
         * @param player メニューを開くプレイヤー
         * @return メニューアイテムのリスト
         */
        fun getRadiusList(player: Player): List<MenuItem> {
            return listOf(
                    MenuItem("すべてのワールド", { app.onRadiusWorldMenuClick("#global", player) }, Material.DIRT),
                    MenuItem("メインワールド", { app.onRadiusWorldMenuClick("#main", player) }, Material.CRAFTING_TABLE),
                    MenuItem("共有ワールド", { app.onRadiusWorldMenuClick("#wildarea2", player) }, Material.GRASS_BLOCK),
                    MenuItem("共有ネザー", { app.onRadiusWorldMenuClick("#wildarea2_nether", player) }, Material.NETHERRACK),
                    MenuItem("共有エンド", { app.onRadiusWorldMenuClick("#wildarea2_the_end", player) }, Material.END_STONE),
                    MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
            )
        }

        /**
         * ～前までの取得か期間での取得かを選択するメニューリストを返す
         *
         * @param player メニューを開くプレイヤー
         * @return メニューアイテムのリスト
         */
        fun getDuringModeList(player: Player): List<MenuItem> {
            return listOf(
                    MenuItem("とある日時まで遡る", { app.onSelectDuringMode(false, player) }, Material.CLOCK),
                    MenuItem("期間を指定して取得する", { app.onSelectDuringMode(true, player) }, Material.CLOCK),
                    MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
            )
        }

        /**
         * アクションを選択するメニューリストを返す
         *
         * @param player メニューを開くプレイヤー
         * @return メニューアイテムのリスト
         */
        fun getActionMenuList(player: Player): List<MenuItem> {
            return listOf(
                    MenuItem("チェスト", { app.onActionMenuClick("container", player) }, Material.CHEST_MINECART),
                    MenuItem("ブロック", { app.onActionMenuClick("block", player) }, Material.GRASS_BLOCK),
                    MenuItem("アイテム", { app.onActionMenuClick("item", player) }, Material.WHEAT_SEEDS),
                    MenuItem("クリック", { app.onActionMenuClick("click", player) }, Material.BOOK),
                    MenuItem("コマンド", { app.onActionMenuClick("command", player) }, Material.PLAYER_HEAD),
                    MenuItem("キル", { app.onActionMenuClick("kill", player) }, Material.ZOMBIE_HEAD),
                    MenuItem("ユーザーネーム", { app.onActionMenuClick("kill", player) }, Material.WRITABLE_BOOK),
                    MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
            )
        }

        /**
         * アクションのオプションを選択するメニューリストを返す
         *
         * @param player メニューを開くプレイヤー
         * @param action 選択したアクションの文字列
         * @return メニューアイテムのリスト
         */
        fun getOptionMenuList(player: Player, action: String): List<MenuItem> {
            return when (action) {
                "container" -> {
                    listOf(
                            MenuItem("アイテムを入れた", { app.onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                            MenuItem("アイテムをとった", { app.onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                            MenuItem("指定しない", { app.onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                            MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
                    )

                }

                "block" -> {
                    listOf(
                            MenuItem("ブロックを設置した", { app.onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                            MenuItem("ブロックを破壊した", { app.onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                            MenuItem("指定しない", { app.onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                            MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
                    )

                }

                "item" -> {
                    listOf(
                            MenuItem("アイテムの拾得", { app.onOptionMenuClick("+$action", player) }, Material.REDSTONE),
                            MenuItem("アイテムのドロップ", { app.onOptionMenuClick("-$action", player) }, Material.REDSTONE_TORCH),
                            MenuItem("指定しない", { app.onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                            MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
                    )

                }

                "click",
                "command",
                "kill",
                "username" -> {
                    listOf(
                            MenuItem("実行", { app.onOptionMenuClick(action, player) }, Material.REDSTONE),
                            MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
                    )

                }
                // ここに来る想定はないが、現状ここでescapeで戻ることしかできないため、仮の値を作っておく
                else -> {
                    listOf(
                            MenuItem("指定しない", { app.onOptionMenuClick(action, player) }, Material.REDSTONE_LAMP),
                            MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
                    )
                }
            }
        }
    }

}