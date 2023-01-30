package work.xeltica.craft.core.modules.eventFarm

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import work.xeltica.craft.core.utils.Ticks
import kotlin.math.max

class EventFarmHandler : Listener {
    @EventHandler
    fun onBreakCrops(e: BlockBreakEvent) {
        if (!isPlaying()) return
        if (!isGamePlayer(e.player)) return
        if (EventFarmModule.countdown > 0) {
            e.isCancelled = true
            return
        }
        val board = EventFarmModule.board
        when (e.block.type) {
            Material.POTATOES -> {
                board[e.player] = (board[e.player] ?: 0) + 1
                EventFarmModule.showStatus(e.player)
            }

            Material.WHEAT -> {
                val minus = max(((board[e.player] ?: 0) * 0.05f).toInt(), 5)
                board[e.player] = (board[e.player] ?: 0) - minus
                e.player.sendTitle("${ChatColor.RED}小麦を取ってしまった！", "${minus}ポイント減点。", 0, 80, 0)
                e.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, Ticks.from(3.0), 3, false, false, false))
                e.player.world.playSound(e.player.location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 1f)
                // e.player.world.createExplosion(e.player.location, 64f, false, false)
                EventFarmModule.showStatus(e.player)
            }

            else -> {
                e.isCancelled = true
            }
        }
        e.isDropItems = false
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (!isPlaying()) return
        if (!isGamePlayer(e.player)) return
        if (!e.hasChangedPosition()) return
        if (EventFarmModule.countdown > 0) {
            e.isCancelled = true
        }
    }

    fun isPlaying() = EventFarmModule.isPlaying

    fun isGamePlayer(p: Player) = EventFarmModule.board.containsKey(p)
}