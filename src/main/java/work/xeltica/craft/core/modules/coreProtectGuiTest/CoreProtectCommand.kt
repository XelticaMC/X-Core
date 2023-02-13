package work.xeltica.craft.core.modules.coreProtectGuiTest

/**
 * メニューで選択した値やコンソールから入力された送られた値をまとめたクラス
 *
 * NOTE: 初期化のためにlateinit を外したが、ちょっといい感じにできるかもしれない
 */
class CoreProtectCommand {
    var prefixCommand: String = ""
    var user: String = ""
    var radius: String = ""
    var date: String = ""
    var action: String = ""
    var firstInputDate: UnitedTime = UnitedTime(0, "")
    var secondInputDate: UnitedTime = UnitedTime(0, "")
}

/**
 * ログを取るときに使う、時間の数値と単位をまとめたもの
 *
 * @param timeValue 時間の数値
 * @param unit 時間の単位
 */
data class UnitedTime(var timeValue: Int, var unit: String)