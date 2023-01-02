package work.xeltica.craft.core.modules.fly

import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase

object FlyModule : ModuleBase() {
    override fun onEnable() {
        FlyingObserver().runTaskTimer(XCorePlugin.instance, 0, 4)
    }
}