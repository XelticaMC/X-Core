package work.xeltica.craft.core.api

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.commands.CommandBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.modules.xphone.XphoneModule
import work.xeltica.craft.core.xphone.apps.AppBase

/**
 * モジュールの基底クラス。
 */
abstract class ModuleBase {
    open fun onEnable() {}
    open fun onPostEnable() {}
    open fun onDisable() {}

    protected fun registerCommand(name: String, command: CommandBase) {
        CommandRegistry.register(name, command)
    }

    protected fun registerHandler(handler: Listener) {
        Bukkit.getPluginManager().registerEvents(handler, XCorePlugin.instance)
    }

    protected fun registerXPhoneApp(app: AppBase) {
        XphoneModule.registerApp(app)
    }
}