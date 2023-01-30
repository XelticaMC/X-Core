package work.xeltica.craft.core.modules.motd

import work.xeltica.craft.core.api.ModuleBase

/**
 * プレイヤー参加時・退出時のメッセージをカスタムする機能を提供します。
 */
object MotdModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(MotdHandler())
    }
}