package work.xeltica.craft.core.modules.gamemodeChange

import org.bukkit.Bukkit
import work.xeltica.craft.core.api.ModuleBase

object GamemodeChangeModule : ModuleBase() {

    override fun onEnable() {
        Bukkit.getLogger().info("モジュールをローンチしてダーン")
    }

}