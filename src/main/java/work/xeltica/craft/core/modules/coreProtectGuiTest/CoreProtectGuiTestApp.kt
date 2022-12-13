package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリ
 */
class CoreProtectGuiTestApp : AppBase() {

    /**
     * コマンドの値として入れる値
     */
    private val coreProtectCommand by lazy { CoreProtectCommand() }

    /**
     * [CoreProtectGuiTestModule]
     *
     * NOTE: アプリとモジュールでのやりとりがかなり多いのでインスタンス生成処理の削減のためにメンバ変数として宣言する
     */
    private val module by lazy { CoreProtectGuiTestModule }

    /**
     * 範囲を指定するときにつかう
     */
    private lateinit var firstDateTime: Pair<Int, String>
    private lateinit var secondDateTime: Pair<Int, String>

    override fun getName(player: Player): String {
        return "CoreProtectGUIテストアプリ"
    }

    override fun getIcon(player: Player): Material {
        return Material.ENDER_EYE
    }

    override fun onLaunch(player: Player) {
        module.start(player)
    }

    /**
     * 1度目に取得した日時と2度目に取得した日時を比較して
     * [CoreProtectCommand.date]に格納する文字列を決定するやつ
     */
    private fun checkDuringTime() {
        val firstDate = firstDateTime.first.toLong() * convertUnit(firstDateTime.second)
        val lastDate = secondDateTime.first.toLong() * convertUnit(secondDateTime.second)

        if (firstDate < lastDate) {
            coreProtectCommand.date =
                    (firstDateTime.first.toString() + firstDateTime.second) + "-" +
                            (secondDateTime.first.toString() + secondDateTime.second)
            Bukkit.getLogger().info("FirstDate more than")
        } else if (firstDate > lastDate) {
            coreProtectCommand.date =
                    (secondDateTime.first.toString() + secondDateTime.second) + "-" +
                            (firstDateTime.first.toString() + firstDateTime.second)
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
        sendCommand.append("coreprotect:co lookup ")

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

    /**
     * プレイヤー名を入力したら呼ばれる
     *
     * @param userName 入力したプレイヤー名
     * @param player メニューを開いたプレイヤー
     */
    fun onInputPlayerName(userName: String, player: Player) {
        coreProtectCommand.user = userName
        val radiusWorldList = module.getRadiusList(player)

        module.showMenu(player, "ワールドの範囲を指定してください", radiusWorldList)
    }

    /**
     * ワールド範囲を選択したら呼ばれる
     *
     * @param radius ログを取るワールドの範囲
     * @param player メニューを開いたプレイヤー
     */
    fun onRadiusWorldMenuClick(radius: String, player: Player) {
        coreProtectCommand.radius = radius

        val duringModeList = module.getDuringModeList(player)
        module.showMenu(player, "時間の指定方法を選択してください", duringModeList)
    }

    /**
     * 期間指定かどうかを選択したら呼ばれる
     *
     * @param duringModeFlag 選択結果
     * @param player メニューを開いたプレイヤー
     */
    fun onSelectDuringMode(duringModeFlag: Boolean, player: Player) {
        if (duringModeFlag) {
            module.getTimeValueAndUnit(player, true)
        } else {
            module.inputTime(player)
        }
    }

    /**
     * 時間の単位を選択したら呼ばれる(伝播されてくる)
     *
     * @param dateTime 時間と単位をPairでまとめたやつ
     * @param flag 1個目の日時選択の場合はtrue
     * @param player メニューを開いたプレイヤー
     */
    fun onDuringTimeUnit(player: Player, dateTime: Pair<Int, String>, flag: Boolean) {
        if (flag) {
            firstDateTime = dateTime
            module.getTimeValueAndUnit(player, false)
        } else {
            secondDateTime = dateTime
            checkDuringTime()
            val actionMenuList = module.getActionMenuList(player)
            module.showMenu(player, "アクションの種類を選択してください", actionMenuList)
        }
    }

    /**
     * 時間の単位を選択したら呼ばれる
     *
     * @param value 数値
     * @param unit  メニューで選択した時間の単位
     * @param player メニューを開いたプレイヤー
     */
    fun onTimeUnitMenuClick(value: Int, unit: String, player: Player) {
        coreProtectCommand.date = value.toString() + unit
        val actionMenuList = module.getActionMenuList(player)
        module.showMenu(player, "アクションの種類を選択してください", actionMenuList)
    }

    /**
     * action の種類を選択したら呼ばれる
     *
     * @param action メニューから指定したアクション
     * @param player メニューを開いたプレイヤー
     */
    fun onActionMenuClick(action: String, player: Player) {
        Bukkit.getLogger().info(action)
        val optionMenuList = module.getOptionMenuList(player, action)

        module.showMenu(player, "オプションを選択してください", optionMenuList)
    }

    /**
     * actionのオプションを選択したら呼ばれる
     *
     * @param action 最終的にコマンドに付与するアクションの形
     * @param player メニューを開いたプレイヤー
     */
    fun onOptionMenuClick(action: String, player: Player) {
        coreProtectCommand.action = action
        commandSend(player)
    }

    /**
     * 処理を中断するために作ったやつ
     *
     * @param player メニューを開いたプレイヤー
     */
    fun onCancel(player: Player) {
        player.sendMessage("処理をキャンセルします")
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