package work.xeltica.craft.core.api

/**
 * 連携フックの基底クラス。
 */
abstract class HookBase {
    abstract val isEnabled: Boolean
    open fun onEnable() {}
    open fun onDisable() {}
}