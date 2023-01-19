package work.xeltica.craft.core.modules.punishment

import work.xeltica.craft.core.api.ModuleBase

/**
 * 処罰機能を提供するモジュールです。
 */
object PunishmentModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("report", CommandReport())
    }
}