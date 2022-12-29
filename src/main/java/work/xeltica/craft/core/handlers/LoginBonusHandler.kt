package work.xeltica.craft.core.handlers

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.events.RealTimeNewDayEvent
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule.tryGive
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.utils.Ticks.from

class LoginBonusHandler : Listener {
    @EventHandler
    fun onLoginBonus(e: RealTimeNewDayEvent?) {
        val records = PlayerStore.openAll()

        // ログボ記録を削除
        records.forEach{
            it.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS)
            it.delete(PlayerDataKey.RECEIVED_LOGIN_BONUS_SUMMER)
        }

        // いる人にログボ
        Bukkit.getOnlinePlayers().forEach { giveLoginBonus(it) }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        giveLoginBonus(e.player)
    }

    private fun giveLoginBonus(p: Player) {
        val record = PlayerStore.open(p)
        if (!record.getBoolean(PlayerDataKey.RECEIVED_LOGIN_BONUS)) {
            Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
                if (!p.isOnline) return@Runnable
                tryGive(p, LOGIN_BONUS_EBIPOWER)
                p.sendMessage("§a§lログインボーナス達成！§6" + LOGIN_BONUS_EBIPOWER + "EP§fを手に入れた！")
                p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 2f)
                PlayerStore.open(p)[PlayerDataKey.RECEIVED_LOGIN_BONUS] = true
            }, from(2.0).toLong())
        }
    }

    private val LOGIN_BONUS_EBIPOWER = 250
}