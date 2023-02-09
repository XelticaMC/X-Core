package work.xeltica.craft.core.modules.moderation

import work.xeltica.craft.core.api.ModuleBase


object ModerationModule : ModuleBase() {
    override fun onEnable() {
        registerHandler(ModerationHandler())
    }
}