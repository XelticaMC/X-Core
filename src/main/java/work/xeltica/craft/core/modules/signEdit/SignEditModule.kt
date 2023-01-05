package work.xeltica.craft.core.modules.signEdit

import work.xeltica.craft.core.api.ModuleBase

/**
 * 看板を編集できるコマンド signedit を提供するモジュールです。
 */
object SignEditModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("signedit", CommandSignEdit())
    }
}