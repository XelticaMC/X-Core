package work.xeltica.craft.core.modules.loginBonus

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.events.RealTimeNewDayEvent

class LoginBonusHandler : Listener {
    @EventHandler
    fun onLoginBonus(e: RealTimeNewDayEvent) {
        val records = PlayerStore.openAll()

        // ログボ記録を削除
        records.forEach{
            it.delete(LoginBonusModule.isReceivedLoginBonus)
            it.delete(LoginBonusModule.isReceivedSummerLoginBonus)
        }

        // いる人にログボ
        Bukkit.getOnlinePlayers().forEach(LoginBonusModule::giveLoginBonus)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        LoginBonusModule.giveLoginBonus(e.player)
    }
}