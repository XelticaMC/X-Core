package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリ
 */
class CoreProtectGuiTestApp : AppBase() {

    /**
     * インスタンスを使いまわすやつ
     */
    private val gui by lazy { Gui.getInstance() }

    override fun getName(player: Player): String {
        return "CoreProtectGUIテストアプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.ENDER_EYE
    }

    override fun onLaunch(player: Player) {
        showMenu(player, "選択する方法を選択してください",
                listOf(
                        MenuItem("ログを検索する", { lookupModeStart(player) }, Material.WRITABLE_BOOK),
                        MenuItem("ロールバック", { rollbackModeStart(player) }, Material.ENDER_CHEST),
                        MenuItem("ページを取得する", { inputLookUpPage(player) }, Material.BOOK),
                        MenuItem("インスペクトモード切り替え", { player.performCommand("co i") }, Material.ENDER_EYE),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
        )
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
     * ログを検索する
     *
     * @param player ログを取りにいくプレイヤー
     */
    private fun lookupModeStart(player: Player) {
        val command = CoreProtectCommand()
        command.prefixCommand = "co lookup"
        showMenu(player, "ログを取るプレイヤーを指定", chooseHowToGetPlayerName(player, command))
    }

    /**
     * ロールバックを実行する
     *
     * @param player ロールバックを実行するプレイヤー
     */
    private fun rollbackModeStart(player: Player) {
        val command = CoreProtectCommand()
        command.prefixCommand = "co rollback"
        showMenu(player, "どのプレイヤーの行動をロールバックするのかを指定", chooseHowToGetPlayerName(player, command))
    }

    /**
     * プレイヤーネームの取得方法を選択するメニューアイテムのリストを返す
     *
     * @param [player] メニューを開くプレイヤー
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @return メニューアイテムのリスト
     */
    private fun chooseHowToGetPlayerName(player: Player, command: CoreProtectCommand): List<MenuItem> {
        return listOf(
                MenuItem("コンソール入力で取得する", { getInputTextByConsole(player, command) }, Material.WRITABLE_BOOK),
                MenuItem("現在いるプレイヤーから取得する", { getChooseByPlayerMenu(player, command) }, Material.BOOK),
                MenuItem("クリーパー", { onInputPlayerName("#creeper", command, player) }, Material.CREEPER_HEAD),
                MenuItem("TNT", { onInputPlayerName("#tnt", command, player) }, Material.TNT),
                MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
        )
    }

    /**
     * テキストでログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     */
    private fun getInputTextByConsole(player: Player, command: CoreProtectCommand) {
        gui.openTextInput(player, "プレイヤーのIDを指定してください") { userName ->
            onInputPlayerName(userName, command, player)
        }
        return
    }

    /**
     * メニューからログを取るプレイヤーを指定する
     *
     * @param player コマンドを打つプレイヤー
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     */
    private fun getChooseByPlayerMenu(player: Player, command: CoreProtectCommand) {
        gui.openPlayersMenu(player, "プレイヤーを指定してください", { playerName ->
            playerName.player?.name?.let {
                onInputPlayerName(it, command, player)
            }
        }, { it.uniqueId != player.uniqueId || it.uniqueId == player.uniqueId })
        return
    }

    /**
     * プレイヤー名を入力したら呼ばれる
     *
     * @param userName 入力したプレイヤー名
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onInputPlayerName(userName: String, command: CoreProtectCommand, player: Player) {
        command.user = userName

        showMenu(player, "ワールドの範囲を指定してください",
                listOf(
                        MenuItem("すべてのワールド", { onRadiusWorldMenuClick("#global", command, player) }, Material.DIRT),
                        MenuItem("メインワールド", { onRadiusWorldMenuClick("#main", command, player) }, Material.CRAFTING_TABLE),
                        MenuItem("共有ワールド", { onRadiusWorldMenuClick("#wildarea2", command, player) }, Material.GRASS_BLOCK),
                        MenuItem("共有ネザー", { onRadiusWorldMenuClick("#wildarea2_nether", command, player) }, Material.NETHERRACK),
                        MenuItem("共有エンド", { onRadiusWorldMenuClick("#wildarea2_the_end", command, player) }, Material.END_STONE),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
        )
    }

    /**
     * ワールド範囲を選択したら呼ばれる
     *
     * @param radius ログを取るワールドの範囲
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onRadiusWorldMenuClick(radius: String, command: CoreProtectCommand, player: Player) {
        command.radius = radius

        showMenu(player, "時間の指定方法を選択してください",
                listOf(
                        MenuItem("とある日時まで遡る", { onSelectDuringMode(false, command, player) }, Material.CLOCK),
                        MenuItem("期間を指定して取得する", { onSelectDuringMode(true, command, player) }, Material.CLOCK),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
        )
    }

    /**
     * 期間指定かどうかを選択したら呼ばれる
     *
     * @param duringModeFlag 選択結果
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onSelectDuringMode(duringModeFlag: Boolean, command: CoreProtectCommand, player: Player) {
        if (duringModeFlag) {
            inputDuringTime(true, command, player)
        } else {
            inputDuringTime(false, command, player)
        }
    }

    /**
     * 時間の単位を選択したら呼ばれる(伝播されてくる)
     *
     * @param player メニューを開いたプレイヤー
     * @param dateTime 時間と単位をPairでまとめたやつ
     * @param flag 1個目の日時選択の場合はtrue
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     */
    private fun onDuringTimeUnit(player: Player, dateTime: Pair<Int, String>, flag: Boolean, command: CoreProtectCommand) {
        if (flag) {
            command.firstInputDate = dateTime
            inputDuringTime(false, command, player)
        } else {
            command.secondInputDate = dateTime
            showMenu(player, "アクションの種類を選択してください",
                    listOf(
                            MenuItem("チェスト", { onActionMenuClick("container", command, player) }, Material.CHEST_MINECART),
                            MenuItem("ブロック", { onActionMenuClick("block", command, player) }, Material.GRASS_BLOCK),
                            MenuItem("アイテム", { onActionMenuClick("item", command, player) }, Material.WHEAT_SEEDS),
                            MenuItem("チャット", { onActionMenuClick("chat", command, player) }, Material.JUKEBOX),
                            MenuItem("クリック", { onActionMenuClick("click", command, player) }, Material.BOOK),
                            MenuItem("コマンド", { onActionMenuClick("command", command, player) }, Material.PLAYER_HEAD),
                            MenuItem("キル", { onActionMenuClick("kill", command, player) }, Material.ZOMBIE_HEAD),
                            MenuItem("ユーザーネーム", { onActionMenuClick("username", command, player) }, Material.WRITABLE_BOOK),
                            MenuItem("指定しない", { onActionMenuClick(command, player) }, Material.REDSTONE_TORCH),
                            MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                    )
            )
        }
    }

    /**
     * 「指定しない」を選択したら呼ばれる
     *
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onActionMenuClick(command: CoreProtectCommand, player: Player) {
        CoreProtectGuiTestModule.commandSend(player, command)
    }

    /**
     * action の種類を選択したら呼ばれる
     *
     * @param action メニューから指定したアクション
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onActionMenuClick(action: String, command: CoreProtectCommand, player: Player) {
        showMenu(player, "オプションを選択してください", getOptionMenuList(player, action, command))
    }

    /**
     * actionのオプションを選択したら呼ばれる
     *
     * @param action 最終的にコマンドに付与するアクションの形
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player メニューを開いたプレイヤー
     */
    private fun onOptionMenuClick(action: String, command: CoreProtectCommand, player: Player) {
        command.action = action
        CoreProtectGuiTestModule.commandSend(player, command)
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
     * ページ番号を入力させる
     *
     * @param player コマンドを打ちたいプレイヤー
     */
    private fun inputLookUpPage(player: Player) {
        val command = CoreProtectCommand()
        command.prefixCommand = "co lookup "
        gui.openTextInput(player, "閲覧するページを選択してください") { inputString ->
            val value = inputString.toIntOrNull()
            if (value == null || value <= 0) {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }

            player.performCommand(command.prefixCommand + value)
        }
    }

    /**
     * time の値を入力する(範囲選択用)
     *
     * @param flag 1個目の日時選択の場合はtrue
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @param player 入力をするプレイヤー
     */
    private fun inputDuringTime(flag: Boolean, command: CoreProtectCommand, player: Player) {
        gui.openTextInput(player, "時間の数値(整数)を入力してください。") { inputString ->
            val value = inputString.toIntOrNull()
            if (value == null || value <= 0) {
                Gui.getInstance().error(player, "正しい数値を入力する必要があります。")
                return@openTextInput
            }

            showMenu(player, "時間の単位を選択してください",
                    listOf(
                            MenuItem("週", { onDuringTimeUnit(player, Pair(value, "w"), flag, command) }, Material.CLOCK),
                            MenuItem("日", { onDuringTimeUnit(player, Pair(value, "d"), flag, command) }, Material.CLOCK),
                            MenuItem("時", { onDuringTimeUnit(player, Pair(value, "h"), flag, command) }, Material.CLOCK),
                            MenuItem("分", { onDuringTimeUnit(player, Pair(value, "m"), flag, command) }, Material.CLOCK),
                            MenuItem("秒", { onDuringTimeUnit(player, Pair(value, "s"), flag, command) }, Material.CLOCK),
                            MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                    )
            )
        }
    }

    /**
     * アクションのオプションを選択するメニューリストを返す
     *
     * @param player メニューを開くプレイヤー
     * @param action 選択したアクションの文字列
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     * @return メニューアイテムのリスト
     *
     * NOTE: 一か所しか呼び出さないが、分岐がとにかく長いのでメソッドへ
     */
    private fun getOptionMenuList(player: Player, action: String, command: CoreProtectCommand): List<MenuItem> {
        return when (action) {
            "container" -> {
                listOf(
                        MenuItem("アイテムを入れた", { onOptionMenuClick("+$action", command, player) }, Material.REDSTONE),
                        MenuItem("アイテムをとった", { onOptionMenuClick("-$action", command, player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, command, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }

            "block" -> {
                listOf(
                        MenuItem("ブロックを設置した", { onOptionMenuClick("+$action", command, player) }, Material.REDSTONE),
                        MenuItem("ブロックを破壊した", { onOptionMenuClick("-$action", command, player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, command, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }

            "item" -> {
                listOf(
                        MenuItem("アイテムの拾得", { onOptionMenuClick("+$action", command, player) }, Material.REDSTONE),
                        MenuItem("アイテムのドロップ", { onOptionMenuClick("-$action", command, player) }, Material.REDSTONE_TORCH),
                        MenuItem("指定しない", { onOptionMenuClick(action, command, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }

            "chat",
            "click",
            "command",
            "kill",
            "username" -> {
                listOf(
                        MenuItem("実行", { onOptionMenuClick(action, command, player) }, Material.REDSTONE),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }
            // ここに来る想定はないが、現状ここでescapeで戻ることしかできないため、仮の値を作っておく
            else -> {
                listOf(
                        MenuItem("指定しない", { onOptionMenuClick(action, command, player) }, Material.REDSTONE_LAMP),
                        MenuItem("キャンセル", { onCancel(player) }, Material.BARRIER),
                )
            }
        }
    }

}