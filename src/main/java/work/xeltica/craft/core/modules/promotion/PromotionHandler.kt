package work.xeltica.craft.core.modules.promotion

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.world.WorldModule

/**
 * わかばロール向けの機能制限に関するハンドラーをまとめています。
 * @author Xeltica
 */
class PromotionHandler : Listener {
    private val deniedBlocks: MutableSet<Material> = HashSet()
    private val deniedItems: MutableSet<Material> = HashSet()
    private val deniedTags: Set<Tag<Material>> = HashSet()
    private val deniedItemTags: Set<Tag<Material>> = HashSet()

    init {
        deniedBlocks.add(Material.DISPENSER)
        deniedBlocks.add(Material.PISTON)
        deniedBlocks.add(Material.STICKY_PISTON)
        deniedBlocks.add(Material.TNT)
        deniedBlocks.add(Material.LEVER)
        deniedBlocks.add(Material.REDSTONE_TORCH)
        deniedBlocks.add(Material.SLIME_BLOCK)
        deniedBlocks.add(Material.HONEY_BLOCK)
        deniedBlocks.add(Material.TRIPWIRE_HOOK)
        deniedBlocks.add(Material.TRAPPED_CHEST)
        deniedBlocks.add(Material.DAYLIGHT_DETECTOR)
        deniedBlocks.add(Material.REDSTONE_BLOCK)
        deniedBlocks.add(Material.HOPPER)
        deniedBlocks.add(Material.DROPPER)
        deniedBlocks.add(Material.OBSERVER)
        deniedBlocks.add(Material.FIRE)
        deniedBlocks.add(Material.LAVA)
        deniedBlocks.add(Material.WATER)
        deniedBlocks.add(Material.CARVED_PUMPKIN)
        deniedBlocks.add(Material.JACK_O_LANTERN)
        deniedBlocks.add(Material.WITHER_SKELETON_SKULL)
        deniedItems.add(Material.TNT_MINECART)
        deniedItems.add(Material.END_CRYSTAL)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val record = PlayerStore.open(e.player)
        if (PromotionModule.isCitizen(e.player)) {
            HintModule.achieve(e.player, Hint.BE_CITIZEN)
        } else if (!record.has(PlayerDataKey.NEWCOMER_TIME)) {
            e.player.sendMessage("総プレイ時間が30分を超えたため、§b市民§rへの昇格ができます！")
            e.player.sendMessage("詳しくは §b/promo§rコマンドを実行してください。")
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (PromotionModule.isCitizen(e.player)) return
        val mat = e.block.type
        if (isDeniedMaterial(mat)) {
            prevent(e, e.player, "ブロック $mat を設置できません。")
        }
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (PromotionModule.isCitizen(e.player)) return
        val mat = e.block.type
        if (isDeniedMaterial(mat)) {
            prevent(e, e.player, "ブロック $mat を破壊できません。")
        }
    }

    @EventHandler
    fun onBlockInteract(e: PlayerInteractEvent) {
        if (PromotionModule.isCitizen(e.player)) return
        val clickedBlock = e.clickedBlock
        if (clickedBlock != null) {
            val mat = e.clickedBlock!!.type
            if (isDeniedMaterial(mat)) {
                prevent(e, e.player, "ブロック $mat を使用できません。")
            }
        }
        val isUsingItem = e.action == Action.RIGHT_CLICK_BLOCK && e.item != null
        if (isUsingItem && isDeniedItemMaterial(e.item!!.type)) {
            prevent(e, e.player, "アイテム " + e.item!!.type + " を使用できません。")
        }
        if (isUsingItem && e.item!!.type.toString().contains("BUCKET")) {
            prevent(e, e.player, "バケツを使用できません。")
        }
    }

    @EventHandler
    fun onEntityDamage(e: EntityDamageByEntityEvent) {
        if (e.damager !is Player) return
        val p = e.damager as Player
        if (PromotionModule.isCitizen(p)) return
        if (e.entityType == EntityType.ENDER_CRYSTAL) {
            prevent(e, p, "エンドクリスタルを破壊できません。")
        }
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        val destInfo = WorldModule.getWorldInfo(e.to.world)
        val player = e.player
        if (destInfo.isCitizenOnly && !player.hasPermission("otanoshimi.citizen")) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 0.5f)
            player.sendMessage("§aわかば§rプレイヤーは§6${destInfo.displayName}§rに行くことができません！\n§b/promo§rコマンドを実行して、昇格方法を確認してください！")
            e.isCancelled = true
            return
        }
    }

    /**
     * メインワールドにおいて、レッドストーンおよび発射物によるTNT爆破のみを無効化する
     * これらを塞がないと、わかばロールでTNTを着火できてしまうため。
     */
    @EventHandler
    fun onTNTPrime(e: TNTPrimeEvent) {
        if (e.block.world.name == "main" && e.reason in listOf(PrimeReason.REDSTONE, PrimeReason.PROJECTILE)) {
            e.isCancelled = true
        }
    }

    private fun prevent(e: Cancellable, p: Player, message: String) {
        e.isCancelled = true
        p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1f, 0.5f)
        p.sendMessage("§aわかば§rプレイヤーは§c$message")
        p.sendMessage("§b市民への昇格§rが必要です。詳しくは§b/promo§rコマンドを実行してください！")
    }

    private fun isDeniedMaterial(mat: Material): Boolean {
        return (deniedBlocks.contains(mat)) || deniedTags.any { it.isTagged(mat) }
    }

    private fun isDeniedItemMaterial(mat: Material): Boolean {
        return (deniedItems.contains(mat)) || deniedItemTags.any { it.isTagged(mat) }
    }
}