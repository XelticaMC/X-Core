package work.xeltica.craft.core.plugins

import org.bukkit.Bukkit
import work.xeltica.craft.core.XCorePlugin

/**
 * X-Core プラグインの基底クラスです。
 * X-Core自体がプラグインなので紛らわしいですが、
 * Vaultのような外部プラグインとの連携のために用意していた子プラグインの仕組みです。
 * あまり使わず直書きしてしまっています…。
 * @author Xeltica
 */
abstract class PluginBase {
    open fun onEnable(plugin: XCorePlugin?) {
        Bukkit.getLogger().info("Enabling sub-plugin ${this.javaClass.name}...")
    }

    open fun onDisable(plugin: XCorePlugin?) {
        Bukkit.getLogger().info("Disabling sub-plugin ${this.javaClass.name}...")
    }
}