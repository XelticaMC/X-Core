package work.xeltica.craft.core.modules.eventFinal

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import java.util.UUID

object EventFinalModule : ModuleBase() {
    val COUNTER_NAME = "final"

    val sessions = mutableMapOf<UUID, Session>()

    override fun onEnable() {
        sessions.clear()
        registerHandler(EventFinalHandler())
        EventFinalWorker().runTaskTimer(XCorePlugin.instance, 0, 1)
    }

    fun updatePhase(player: Player, phase: Phase) {
        val session = sessions[player.uniqueId] ?: return
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

    fun retire(player: Player) {
        val record = PlayerStore.open(player)
        val count = record.getInt(CounterModule.PS_KEY_COUNT)

        val rechallengeText = "本日は" + (if (count == 0) "あと1回再チャレンジできます。" else "もう再チャレンジできません。")

        Gui.getInstance().openDialog(player, "リタイアしますか？", "リタイアすると、記録が失われた状態で始めの地点に戻ります。$rechallengeText" , {
            record.delete(CounterModule.PS_KEY_ID)
            record.delete(CounterModule.PS_KEY_TIME)
            record[CounterModule.PS_KEY_COUNT] = record.getInt(CounterModule.PS_KEY_COUNT, 0) + 1
            sessions.remove(player.uniqueId)

            player.sendMessage("カウントダウンをリタイアしました。$rechallengeText")
            NbsModule.stopRadio(player)
            finalize(player, true)
        }, "リタイアする")
    }

    fun finalize(player: Player, withRespawn: Boolean = false) {
        // 乗り物を削除
        player.vehicle?.remove()

        // アイテム欄を削除
        player.inventory.clear()

        if (withRespawn) {
            player.teleport(player.world.spawnLocation)
        }
    }
}