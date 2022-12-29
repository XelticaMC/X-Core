package work.xeltica.craft.core.api

abstract class HookBase {
    abstract val isEnabled: Boolean
    open fun onEnable() {}
    open fun onDisable() {}
}