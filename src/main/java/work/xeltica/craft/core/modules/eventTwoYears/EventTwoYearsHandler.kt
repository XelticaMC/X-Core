package work.xeltica.craft.core.modules.eventTwoYears

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.counter.PlayerCounterFinish
import work.xeltica.craft.core.modules.counter.PlayerCounterStart
import work.xeltica.craft.core.modules.nbs.NbsModel
import work.xeltica.craft.core.modules.nbs.NbsModule

class EventTwoYearsHandler : Listener {
    /**
     * タイムアタック中は溶岩ダイブによりチェックポイントへ移動する
     */
    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        val store = PlayerStore.open(player)
        val counterName = store.getString(CounterModule.PS_KEY_ID)
        // イベントワールドでなければreturn
        if (player.world.name != "event") return
        // イベント用のカウンターをプレイ中でなければreturn
        if (counterName != EventTwoYearsModule.EVENT_COUNTER_ID) return

        e.isCancelled = true
        if (e.cause !== EntityDamageEvent.DamageCause.LAVA) return

        player.teleport(EventTwoYearsModule.getCheckPoint(player))
        player.fireTicks = 0
        EventTwoYearsModule.incrementDeathCount(player)
        player.playSound(player, Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1f, 1f)
    }

    @EventHandler
    fun onClickBed(e: PlayerInteractEvent) {
        if (e.action === Action.PHYSICAL) return
        val block = e.clickedBlock ?: return
        val store = PlayerStore.open(e.player)
        val counterName = store.getString(CounterModule.PS_KEY_ID)
        // イベント用のカウンターをプレイ中でなければreturn
        if (counterName != EventTwoYearsModule.EVENT_COUNTER_ID) return
        // ベッドでなければ無視
        if (!Tag.BEDS.isTagged(block.type)) return

        e.isCancelled = true

        val checkpoint = block.location.add(0.0, 1.0, 0.0)
        checkpoint.yaw = e.player.location.yaw
        EventTwoYearsModule.setCheckpoint(e.player, checkpoint)
        e.player.world.spawnParticle(Particle.COMPOSTER, checkpoint, 16, 0.3, 0.5, 0.3, 0.1)
        e.player.playSound(e.player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1f, 1f)
        e.player.sendMessage("${ChatColor.GREEN}${ChatColor.ITALIC}チェックポイントを設置した！")
    }

    /**
     * タイムアタック開始時に音を鳴らす機能
     */
    @EventHandler
    fun onCounterStart(e: PlayerCounterStart) {
        val player = e.player
        if (e.counter.name != EventTwoYearsModule.EVENT_COUNTER_ID) return
        NbsModule.playRadio(player, "csikospost", NbsModel.PlaybackMode.LOOP)
    }

    /**
     * イベントマップ：TA終了イベント
     */
    @EventHandler
    fun onCounterFinish(e: PlayerCounterFinish) {
        val player = e.player
        if (e.counter.name != EventTwoYearsModule.EVENT_COUNTER_ID) return
        NbsModule.stopRadio(player)
        val deathCount = PlayerStore.open(player).getInt(EventTwoYearsModule.PS_KEY_DEATH_COUNT, 0)
        val checkpoint = PlayerStore.open(player).getLocation(EventTwoYearsModule.PS_KEY_CHECKPOINT)
        if (deathCount == 0 && checkpoint == null) {
            HintModule.achieve(player, Hint.TWO_YEARS_EVENT_NO_MISS)
        }
        EventTwoYearsModule.resetPlayerStore(player)
    }
}