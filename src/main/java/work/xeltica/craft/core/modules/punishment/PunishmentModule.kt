package work.xeltica.craft.core.modules.punishment

import work.xeltica.craft.core.api.ModuleBase

object PunishmentModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("report", CommandReport())
    }
}