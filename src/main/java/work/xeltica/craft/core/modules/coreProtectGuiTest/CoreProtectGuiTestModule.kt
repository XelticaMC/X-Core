package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリのモジュール
 */
object CoreProtectGuiTestModule : ModuleBase() {
    override fun onEnable() {
        // むかつくので消さない
        Bukkit.getLogger().info("モジュールをボンジュール～")
    }

    /**
     * 1度目に取得した日時と2度目に取得した日時を比較して
     * [CoreProtectCommand.date]に格納する文字列を決定するやつ
     */
    private fun checkDuringTime(command: CoreProtectCommand): String {
        if (command.firstInputDate.timeValue == 0) {
            return command.secondInputDate.timeValue.toString() + command.secondInputDate.unit
        }

        val firstDate = command.firstInputDate.timeValue.toLong() * convertUnit(command.firstInputDate.unit)
        val lastDate = command.secondInputDate.timeValue.toLong() * convertUnit(command.secondInputDate.unit)

        return if (firstDate < lastDate) {
            (command.firstInputDate.timeValue.toString() + command.firstInputDate.unit) + "-" + (command.secondInputDate.timeValue.toString() + command.secondInputDate.unit)
        } else if (firstDate > lastDate) {
            (command.secondInputDate.timeValue.toString() + command.secondInputDate.unit) + "-" + (command.firstInputDate.timeValue.toString() + command.firstInputDate.unit)

        } else {
            // ここに到達する場合は同値である場合なので、onTimeUnitMenuClick と同じ経路になるような形で
            // コマンドの文字列を確定させる
            command.firstInputDate.timeValue.toString() + command.firstInputDate.unit
        }
    }

    /**
     * 単位を秒数に変換する
     *
     * @param unit 単位
     * @return 単位を秒数に変換した数値。値がでかくなるので[Long]で変換
     */
    private fun convertUnit(unit: String): Long {
        return when (unit) {
            "s" -> 1
            "m" -> 1 * 60
            "h" -> 1 * 60 * 60
            "d" -> 1 * 60 * 60 * 24
            "w" -> 1 * 60 * 60 * 24 * 7
            else -> throw IllegalArgumentException("Invalid unit \"${unit}\"")
        }
    }

    /**
     * コマンドをコンソールに送る
     *
     * @param player コマンドを送るプレイヤー
     * @param [command] 伝播させている[CoreProtectCommand]のインスタンス
     */
    fun commandSend(player: Player, command: CoreProtectCommand) {
        val sendCommand = StringBuilder()
        command.date = checkDuringTime(command)
        sendCommand.append(command.prefixCommand + " ")

        // あくまで空文字だったらコマンドのパラメーターから除外するって処理なので、ここら辺はうまい具合にできそうかも
        if (command.user.isNotBlank()) {
            sendCommand.append("user:" + command.user + " ")
        }
        if (command.radius.isNotBlank()) {
            sendCommand.append("radius:" + command.radius + " ")
        }
        if (command.date.isNotBlank()) {
            sendCommand.append("time:" + command.date + " ")
        }
        if (command.action.isNotBlank()) {
            sendCommand.append("action:" + command.action + " ")
        }

        Bukkit.getLogger().info(sendCommand.toString())
        player.performCommand(sendCommand.toString())
    }
}