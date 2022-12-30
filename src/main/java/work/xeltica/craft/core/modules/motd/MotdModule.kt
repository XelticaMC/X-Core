package work.xeltica.craft.core.modules.motd

import work.xeltica.craft.core.api.ModuleBase

object MotdModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(MotdHandler())
    }
}