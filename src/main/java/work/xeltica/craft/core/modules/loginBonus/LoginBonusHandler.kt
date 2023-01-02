package work.xeltica.craft.core.modules.loginBonus

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore

class LoginBonusHandler : Listener {
    @EventHandler
    fun onLoginBonus(e: RealTimeNewDayEvent) {
        // ログボ記録を削除
        PlayerStore.openAll().forEach {
            it.delete(LoginBonusModule.PS_KEY_LOGIN_BONUS)
        }

        // いる人にログボ
        Bukkit.getOnlinePlayers().forEach(LoginBonusModule::giveLoginBonus)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        LoginBonusModule.giveLoginBonus(e.player)
    }
}