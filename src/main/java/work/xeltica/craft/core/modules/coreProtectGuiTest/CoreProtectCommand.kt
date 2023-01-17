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
    var firstInputDate: Pair<Int, String> = Pair(0, "")
    var secondInputDate: Pair<Int, String> = Pair(0, "")
}