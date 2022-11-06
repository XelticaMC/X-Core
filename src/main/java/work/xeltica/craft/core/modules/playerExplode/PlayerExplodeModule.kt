package work.xeltica.craft.core.modules.playerExplode

import org.bukkit.Bukkit
import work.xeltica.craft.core.api.ModuleBase

object PlayerExplodeModule : ModuleBase() {
    override fun onEnable() {
        Bukkit.getLogger().info("モジュールが読み込まれました")
    }
}