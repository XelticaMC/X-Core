package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase
import java.lang.StringBuilder

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリ
 */
class CoreProtectGuiTestApp : AppBase() {

    /**
     * コマンドの値として入れる値
     *
     * NOTE: 本当はby lazy 使いたい
     */
    private val coreProtectCommand = CoreProtectCommand()

    /**
     * [CoreProtectGuiTestModule]
     *
     * NOTE: アプリとモジュールでのやりとりがかなり多いのでインスタンス生成処理の削減のためにメンバ変数として宣言する
     */
    private val module by lazy { CoreProtectGuiTestModule }

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
     * コマンドをコンソールに送る
     *
     * @param player コマンドを送るプレイヤー
     */
    private fun commandSend(player: Player) {
        val sendCommand = StringBuilder()
        sendCommand.append("/co l ")

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
        coreProtectCommand.date = ""

        module.inputTime(player)
    }

    /**
     * 期間指定かどうかを選択したら呼ばれる
     *
     * @param duringModeFlag 選択結果
     * @param player メニューを開いたプレイヤー
     */
    fun onSelectDuringMode(duringModeFlag: Boolean, player: Player) {
        // TODO 期間指定の場合、1度目に選択した値を保持する必要がありそうなのでどうやって管理させるか思考中
    }

    /**
     * 時間の単位を選択したら呼ばれる
     *
     * @param value 数値
     * @param unit  メニューで選択した時間の単位
     * @param player メニューを開いたプレイヤー
     */
    fun onTimeUnitMenuClick(value: Int, unit: String, player: Player) {
        coreProtectCommand.date = coreProtectCommand.date + value.toString() + unit
        val actionMenuList = module.getActionList(player)
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
        val optionMenuList = module.getOptionList(action, player)

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

    fun onCancel(player: Player){
        player.sendMessage("処理をキャンセルします")
    }

    /**
     * メニューで選択した値やコンソールから入力された送られた値をまとめたクラス
     *
     * NOTE: lateinit var じゃなくてもいいかもしれない
     * (by lazyを使いたかったけどうまい具合に値をセットできなさそうだから一旦lateinit varで運用)
     */
    private class CoreProtectCommand {
        lateinit var user: String
        lateinit var radius: String
        lateinit var date: String
        lateinit var action: String
    }

}