package work.xeltica.craft.core.modules.signEdit

import work.xeltica.craft.core.api.ModuleBase

object SignEditModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("signedit", CommandSignEdit())
    }
}