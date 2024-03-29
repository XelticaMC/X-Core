package work.xeltica.craft.core.modules.ebipower

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule

/**
 * エビパワーに関するオブザーバー。
 */
class EbipowerObserver : BukkitRunnable() {

    override fun run() {
        if (players.isEmpty()) {
            players = Bukkit.getOnlinePlayers().toList()
            return
        }

        val module = EbiPowerModule
        val player = players[index]

        if (!player.isOnline) return

        val ebipower = module.get(player)
        if (ebipower >= 1000000) {
            HintModule.achieve(player, Hint.EBIPOWER_1000000)
        }
        if (ebipower >= 5000000) {
            HintModule.achieve(player, Hint.EBIPOWER_5000000)
        }
        if (ebipower >= 10000000) {
            HintModule.achieve(player, Hint.EBIPOWER_10000000)
        }

        index++
        if (index >= players.size) {
            players = Bukkit.getOnlinePlayers().toList()
            index = 0
            return
        }
    }

    private var index = 0
    private var players: List<Player> = emptyList()
}