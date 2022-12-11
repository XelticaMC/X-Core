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
     * プレイヤーネームの取得方法を選択するメニューアイテムのリストを返す
     *
     * @param [player] メニューを開くプレイヤー
     */
    private fun chooseHowToGetPlayerName(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("コマンドで取得する", { getInputTextByConsole(player) }, Material.WRITABLE_BOOK),
                MenuItem("現在いるプレイヤーから取得する", { getChooseByPlayerMenu(player) }, Material.BOOK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER ),
        )
    }

    /**
     * ログを取得する単位を選択するメニューリストを返す
     *
     * @param value 単位の前の数値
     * @param player メニューを開くプレイヤー
     */
    private fun getTimeUnitList(value: Int, player: Player): List<MenuItem> {
        return listOf(
                MenuItem("週", { app.onTimeUnitMenuClick(value, "w", player) }, Material.CLOCK),
                MenuItem("時", { app.onTimeUnitMenuClick(value, "h", player) }, Material.CLOCK),
                MenuItem("日", { app.onTimeUnitMenuClick(value, "d", player) }, Material.CLOCK),
                MenuItem("分", { app.onTimeUnitMenuClick(value, "m", player) }, Material.CLOCK),
                MenuItem("秒", { app.onTimeUnitMenuClick(value, "s", player) }, Material.CLOCK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER ),
        )
    }

    /**
     * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリの開始
     *
     * @param player コマンドを打つプレイヤー
     */
    fun start(player: Player) {
        showMenu(player, "ログを取るプレイヤーを指定", chooseHowToGetPlayerName(player))
    }

    /**
     * 渡されたアイテムからメニュー画面を開く
     *
     * @param player メニューを開くプレイヤー
     * @param title 開くメニューのタイトル
     * @param items 開くメニューのアイテム一覧
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

    /**
     * ワールドの範囲を選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
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
     */
    fun getActionMenuList(player: Player): List<MenuItem> {
        return listOf(
                MenuItem("チェスト", { app.onActionMenuClick("container", player) }, Material.CHEST_MINECART),
                MenuItem("ブロック", { app.onActionMenuClick("block", player) }, Material.GRASS_BLOCK),
                MenuItem("キャンセル", { app.onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * アクションのオプションを選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @param action 選択したアクションの文字列
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