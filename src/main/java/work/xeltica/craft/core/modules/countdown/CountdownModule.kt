package work.xeltica.craft.core.modules.countdown

import work.xeltica.craft.core.api.ModuleBase

object CountdownModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("countdown", CommandCountdown())
    }
}