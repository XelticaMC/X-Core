package work.xeltica.craft.core.modules.bossbar

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase

object BossBarModule : ModuleBase() {
    private val bossBars: ArrayList<BossBar> = ArrayList()

    override fun onEnable() {
        registerHandler(BossBarHandler())
    }

    fun add(bar: BossBar) {
        bossBars.add(bar)
        Bukkit.getServer().showBossBar(bar)
    }

    fun remove(bar: BossBar) {
        bossBars.remove(bar)
        Bukkit.getServer().hideBossBar(bar)
    }

    fun applyAll(p: Player) {
        bossBars.forEach(p::showBossBar)
    }
}