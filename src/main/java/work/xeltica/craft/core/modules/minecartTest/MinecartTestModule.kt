package work.xeltica.craft.core.modules.minecartTest

import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase

object MinecartTestModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(MinecartTestHandler())
        MinecartVelocityObserver().runTaskTimer(XCorePlugin.instance, 0L, 10L)
    }
}