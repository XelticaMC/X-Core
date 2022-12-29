package work.xeltica.craft.core.modules.player

import work.xeltica.craft.core.api.ModuleBase

object PlayerModule: ModuleBase() {

    override fun onEnable() {
        registerHandler(PlayerHandler())
    }
}