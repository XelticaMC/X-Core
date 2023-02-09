package work.xeltica.craft.core.modules.livemode

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.bossbar.BossBarModule
import java.util.UUID

/**
 * 配信モード機能を提供するモジュール。
 */
object LiveModeModule : ModuleBase() {
    val liveBarMap = HashMap<UUID, BossBar>()

    override fun onEnable() {
        registerCommand("live", CommandLive())
        registerHandler(LiveModeHandler())
    }

    /**
     * [player] の配信モードを [isLive] に設定します。
     */
    fun setLiveMode(player: Player, isLive: Boolean) {
        if (isLive == isLiveMode(player)) return
        if (isLive) {
            val name = "%s が配信中".format(player.name)
            val bar = BossBar.bossBar(Component.text(name), BossBar.MAX_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS)

            liveBarMap[player.uniqueId] = bar
            BossBarModule.add(bar)
        } else {
            val bar = liveBarMap[player.uniqueId] ?: return

            liveBarMap.remove(player.uniqueId)
            BossBarModule.remove(bar)
        }
    }

    /**
     * [player] が配信モードかどうかを取得します。
     */
    fun isLiveMode(player: Player): Boolean {
        return liveBarMap.containsKey(player.uniqueId)
    }
}