package work.xeltica.craft.core.modules.nightmare

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.Random

/**
 * ナイトメアワールドに関するハンドラーをまとめています。
 *
 * @author Xeltica
 */
class NightmareHandler : Listener {
    private val random = Random()
    private val superRareItems = HashSet<Material>()
    private val superRareItemsDropRatio = 0.1f

    init {
        superRareItems.add(Material.CREEPER_HEAD)
        superRareItems.add(Material.SKELETON_SKULL)
        superRareItems.add(Material.ZOMBIE_HEAD)
        superRareItems.add(Material.PLAYER_HEAD)
        superRareItems.add(Material.WITHER_SKELETON_SKULL)
        superRareItems.add(Material.DRAGON_HEAD)
        superRareItems.addAll(Tag.ITEMS_MUSIC_DISCS.values)
    }

    /**
     * ベッド爆弾を再現する
     */
    @EventHandler
    fun onPlayerUseBed(e: PlayerInteractEvent) {
        val block = e.clickedBlock ?: return
        val loc = block.location
        if (loc.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        if (Tag.BEDS.isTagged(block.type)) {
            block.breakNaturally()
            e.isCancelled = true
            loc.createExplosion(5f, true)
        }
    }

    /**
     * レアアイテムの入手確率を下げる
     */
    @EventHandler
    fun onDropRareItems(e: EntityDeathEvent) {
        if (e.entity.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        if (random.nextDouble() > superRareItemsDropRatio) {
            e.drops.removeIf { superRareItems.contains(it.type) }
        }
    }

    /**
     * ナイトメアでの水没ダメージを抑制
     */
    @EventHandler
    fun onEntityTouchWater(e: EntityDamageEvent) {
        if (e.entity.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        if (e.entityType == EntityType.PLAYER) return
        if (e.cause == EntityDamageEvent.DamageCause.DROWNING) {
            e.isCancelled = true
        }
    }

    /**
     * クリーパーが爆発ダメージで爆発するように
     */
    @EventHandler
    fun onCreeperPrim(e: EntityDamageEvent) {
        if (e.entity.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        if (e.entityType != EntityType.CREEPER) return
        val creeper = e.entity as Creeper
        if (e.cause !in listOf(
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
            )
        ) return

        e.isCancelled = true
        creeper.ignite()
    }

    /**
     * テイム禁止
     */
    @EventHandler
    fun onTryTame(e: EntityTameEvent) {
        if (e.entity.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        e.isCancelled = true
    }

    /**
     * ブロックの設置を禁止
     */
    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (e.player.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        e.setBuild(false)
    }

    /**
     * ブロックの破壊を禁止
     */
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (e.player.world.name == NightmareModule.NIGHTMARE_WORLD_NAME) return
        e.isCancelled = true
    }
}