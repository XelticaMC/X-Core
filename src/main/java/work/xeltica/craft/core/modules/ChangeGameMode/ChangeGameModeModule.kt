package work.xeltica.craft.core.modules.ChangeGameMode

import org.bukkit.Bukkit
import work.xeltica.craft.core.api.ModuleBase

object ChangeGameModeModule: ModuleBase() {
    override fun onEnable() {
        Bukkit.getLogger().info("モジュールが読み込まれました")
    }
}
object