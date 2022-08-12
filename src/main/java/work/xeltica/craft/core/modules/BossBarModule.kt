package work.xeltica.craft.core.modules

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.ArrayList

/**
 * 全体向けに表示するBossBarを管理します。
 * @author Xeltica
 */
object BossBarModule : ModuleBase() {
    override fun onDisable() {
        bossBars.clear()
        super.onDisable()
    }

    fun add(bar: BossBar) {
        bossBars.add(bar)
        Bukkit.getServer().audiences().forEach { a: Audience? -> a!!.showBossBar(bar) }
    }

    fun remove(bar: BossBar) {
        bossBars.remove(bar)
        Bukkit.getServer().audiences().forEach { a: Audience? -> a!!.hideBossBar(bar) }
    }

    fun applyAll(p: Player) {
        bossBars.forEach {
            p.showBossBar(it)
        }
    }

    private val bossBars = ArrayList<BossBar>()
}