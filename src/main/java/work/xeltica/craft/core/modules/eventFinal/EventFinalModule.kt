package work.xeltica.craft.core.modules.eventFinal

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase

object EventFinalModule : ModuleBase() {
    val COUNTER_NAME = "final"

    val sessions = mutableMapOf<Player, Session>()

    override fun onEnable() {
        sessions.clear()
        registerHandler(EventFinalHandler())
        EventFinalWorker().runTaskTimer(XCorePlugin.instance, 0, 1)
    }

    fun updatePhase(player: Player, phase: Phase) {
        val session = sessions[player] ?: return
        session.phase = phase

        val title = when (phase) {
            Phase.GROUND_MARATHON -> "${ChatColor.GREEN}マラソン"
            Phase.BEFORE_LAVA -> "${ChatColor.RED}地底"
            Phase.LAVA_MAZE -> "${ChatColor.BOLD}溶岩迷路"
            Phase.SWIMMING -> "${ChatColor.AQUA}地下水泳"
            Phase.CART -> "${ChatColor.WHITE}トロッコ・エスケープ"
            Phase.BOAT -> "${ChatColor.GREEN}ボート・マラソン"
            else -> null
        }

        val subtitle = when (phase) {
            Phase.GROUND_MARATHON -> "地を駆け、山を登れ！"
            Phase.BEFORE_LAVA -> "順路を進み、${ChatColor.RED}溶岩湖${ChatColor.RESET}へダイブ！"
            Phase.LAVA_MAZE -> "ストライダーを牽引して、ゴールを目指せ！"
            Phase.SWIMMING -> "ゴールまで泳ぎきれ！"
            Phase.CART -> "頭上の的を撃ち抜け！"
            Phase.BOAT -> "このままゴールまで駆け抜けろ！"
            else -> null
        }

        if (title == null || subtitle == null) return
        player.showTitle(Title.title(Component.text(title), Component.text(subtitle)))
        player.sendMessage("$title${ChatColor.RESET} - $subtitle")
    }
}