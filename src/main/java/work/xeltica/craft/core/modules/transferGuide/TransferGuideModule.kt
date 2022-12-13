package work.xeltica.craft.core.modules.transferGuide

import org.bukkit.Bukkit
import work.xeltica.craft.core.api.ModuleBase

object TransferGuideModule : ModuleBase(){
    override fun onEnable() {
        Bukkit.getLogger().info("[TransferGuide] Knit乗換案内を読み込みまいた。")
    }
}