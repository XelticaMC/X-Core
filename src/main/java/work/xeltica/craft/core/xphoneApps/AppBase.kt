package work.xeltica.craft.core.xphoneApps

import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * X Phone アプリのベース。
 * @author Ebise Lutica
 */
abstract class AppBase {
    /**
     * アプリの名前を取得します。
     */
    abstract fun getName(player: Player): String

    /**
     * アプリのアイコンを取得します。
     */
    abstract fun getIcon(player: Player): Material

    /**
     * アプリが表示されるかどうかを取得します。
     */
    open fun isVisible(player: Player): Boolean {
        return true
    }

    /**
     * アプリのアイコンが輝いている（統合版の場合はアプリ名に色がついている）かどうかを取得します。
     */
    open fun isShiny(player: Player): Boolean {
        return false
    }

    /**
     * アプリが実行されたときに呼び出されます。
     */
    abstract fun onLaunch(player: Player)
}
