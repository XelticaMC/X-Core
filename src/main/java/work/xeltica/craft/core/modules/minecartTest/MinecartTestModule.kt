package work.xeltica.craft.core.modules.minecartTest

import work.xeltica.craft.core.api.ModuleBase

object MinecartTestModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(MinecartTestHandler())
    }
}