package work.xeltica.craft.core.modules.ebipower

import com.destroystokyo.paper.MaterialTags
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Ageable
import org.bukkit.entity.Cat
import org.bukkit.entity.Hoglin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Ocelot
import org.bukkit.entity.Player
import org.bukkit.entity.SkeletonHorse
import org.bukkit.entity.Slime
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.hooks.CitizensHook.isCitizensNpc
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataEvent
import work.xeltica.craft.core.modules.world.WorldModule
import java.util.Random

/**
 * エビパワー関連のイベントハンドラをまとめています。
 * @author raink1208
 */
class EbiPowerHandler : Listener {
    companion object {
        private const val HARVEST_POWER_MULTIPLIER = 1
        const val BREAK_BLOCK_BONUS_LIMIT = 4000
    }

    private val crops = HashSet<Material>()
    private val breakBonusList = HashSet<Material>()

    init {
        crops.addAll(Tag.CROPS.values)

        breakBonusList.addAll(Tag.BASE_STONE_OVERWORLD.values)
        breakBonusList.addAll(Tag.BASE_STONE_NETHER.values)
        breakBonusList.addAll(Tag.ICE.values)
        breakBonusList.addAll(Tag.DIRT.values)
        breakBonusList.addAll(Tag.SAND.values)

        breakBonusList.addAll(Tag.COAL_ORES.values)
        breakBonusList.addAll(Tag.IRON_ORES.values)
        breakBonusList.addAll(Tag.COPPER_ORES.values)
        breakBonusList.addAll(Tag.GOLD_ORES.values)
        breakBonusList.addAll(Tag.REDSTONE_ORES.values)
        breakBonusList.addAll(Tag.EMERALD_ORES.values)
        breakBonusList.addAll(Tag.LAPIS_ORES.values)
        breakBonusList.addAll(Tag.DIAMOND_ORES.values)
        breakBonusList.addAll(Tag.LOGS.values)
        breakBonusList.addAll(MaterialTags.TERRACOTTA.values)

        breakBonusList.add(Material.NETHER_QUARTZ_ORE)
        breakBonusList.add(Material.OBSIDIAN)
        breakBonusList.add(Material.ANCIENT_DEBRIS)
        breakBonusList.add(Material.SANDSTONE)
        breakBonusList.add(Material.DEEPSLATE)
        breakBonusList.add(Material.KELP_PLANT)
        breakBonusList.add(Material.DRIPSTONE_BLOCK)
        breakBonusList.add(Material.POINTED_DRIPSTONE)
        breakBonusList.add(Material.POINTED_DRIPSTONE)
        breakBonusList.add(Material.GRAVEL)
        breakBonusList.add(Material.CLAY)
        breakBonusList.add(Material.SOUL_SAND)
        breakBonusList.add(Material.SOUL_SOIL)
        breakBonusList.add(Material.END_STONE)
        breakBonusList.add(Material.CHORUS_FLOWER)
        breakBonusList.add(Material.CHORUS_PLANT)
        breakBonusList.add(Material.PRISMARINE)
    }

    /**
     * プレイヤーが、特別に愛護・庇護すべき動物を殴った場合、ペナルティを生じさせます。
     *
     * ### ネコを殴った場合
     * あまりにもありえない行為のため考えたくもありませんが、
     * そのような人からは100EPを没収します。
     *
     * ### 他者のペットを殴った場合
     * 動物愛護の観点だけでなく、他者の所有物に損害を及ぼしたという点でも10EPを没収します。
     * ただし、スケルトンホースはスケルトンが手懐けている場合もあるため、例外です。
     *
     * ### 子供のモブを殴った場合
     * 子供に危害を加えるのは人として最低な行為です。10EPを没収します。
     * ただし、モンスターの子供は例外です。
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDamageFriendlyCreatures(e: EntityDamageByEntityEvent) {
        val killer = e.damager as? Player ?: return
        val victim = e.entity as? LivingEntity ?: return

        if (victim.fromMobSpawner()) return
        if (victim.isCitizensNpc()) return

        if (victim is Cat || victim is Ocelot) {
            EbiPowerModule.tryTake(killer, 100)
            notification(killer, "可愛い可愛いネコちゃんを殴るなんて！100EPを失った。")
            killer.playSound(killer.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.7f, 0.5f)
            HintModule.achieve(killer, Hint.VIOLENCE_CAT)
        } else if (victim is Tameable && victim.isTamed && victim !is SkeletonHorse) {
            EbiPowerModule.tryTake(killer, 10)
            notification(killer, "ペットを殴るなんて！10EPを失った。")
            killer.playSound(killer.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.7f, 0.5f)
            HintModule.achieve(killer, Hint.VIOLENCE_PET)
        } else if (victim is Ageable && victim !is Monster && victim !is Hoglin && !victim.isAdult) {
            EbiPowerModule.tryTake(killer, 10)
            notification(killer, "子供を殴るなんて！10EPを失った。")
            killer.playSound(killer.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.7f, 0.5f)
            HintModule.achieve(killer, Hint.VIOLENCE_CHILD)
        }
    }

    /**
     * プレイヤーがモブを倒した時にエビパワーを付与します。
     */
    @EventHandler
    fun onPlayerKillMobs(e: EntityDeathEvent) {
        val victim = e.entity
        val killer = e.entity.killer ?: return

        if (playerIsInBlacklisted(killer)) return
        // don't kill cats
        if (victim is Cat || victim is Ocelot) return
        // don't kill tamed pets
        if (victim is Tameable && victim.isTamed && victim !is SkeletonHorse) return
        //don't kill non-monster children
        if (victim is Ageable && victim !is Monster && victim !is Hoglin && !victim.isAdult) return
        // ignore creatures from spawner
        if (victim.fromMobSpawner()) return
        // マグマキューブおよびスライムは大きい個体以外対象外
        if (victim is Slime && victim.size != 4) return
        // ReasonがCUSTOMかCOMMANDであれば対象外とする。他モジュール向け機能
        if (victim.entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM || victim.entitySpawnReason == CreatureSpawnEvent.SpawnReason.COMMAND) return

        var ep = if ("nightmare2" == killer.world.name) EbiPowerModule.getMobDropEP(victim, e) else 6
        val buff = getMobDropBonus(killer.inventory.itemInMainHand) * 4
        ep += if (buff > 0) random.nextInt(buff) else 0
        if (ep > 0) {
            EbiPowerModule.tryGive(killer, ep)
            HintModule.achieve(killer, Hint.KILL_MOB_AND_EARN_MONEY)
        }
    }

    /**
     * 最大まで成長した作物を伐採したとき、エビパワーを付与します。
     * 付与される値は、作物1つにつき（1+幸運の数値）EPです。
     */
    @EventHandler
    fun onHarvestCrops(e: BlockBreakEvent) {
        val p = e.player
        if (playerIsInBlacklisted(p)) return
        val blockData = e.block.blockData
        if (blockData is org.bukkit.block.data.Ageable && blockData.age == blockData.maximumAge) {
            val tool = p.inventory.itemInMainHand
            val bonus = getBlockDropBonus(tool)
            val power = (1 + bonus) * HARVEST_POWER_MULTIPLIER
            EbiPowerModule.tryGive(p, power)
            // もし幸運ボーナスがあれば30%の確率で耐久が減っていく
            if (bonus > 0 && random.nextInt(100) < 30) {
                tool.editMeta {
                    if (it !is Damageable) return@editMeta
                    it.damage += 1
                }
            }
        }
    }

    /**
     * 動物を繁殖させた場合、2EP貰えます。
     */
    @EventHandler
    fun onBreedEntities(e: EntityBreedEvent) {
        val breeder = e.breeder as? Player ?: return
        if (playerIsInBlacklisted(breeder)) return
        EbiPowerModule.tryGive(breeder, 2)
        HintModule.achieve(breeder, Hint.BREED_AND_EARN_MONEY)
    }

    /**
     * ブロックを採掘した場合、ブロックの種別やツルハシの幸運値によってエビパワーを貰えます。
     */
    @EventHandler
    fun onMineBlocks(e: BlockBreakEvent) {
        if (!breakBonusList.contains(e.block.type)) return
        if (playerIsInBlacklisted(e.player)) return
        val record = PlayerStore.open(e.player)
        val brokenBlocksCount = record.getInt(EbiPowerModule.PS_KEY_BROKEN_BLOCKS_COUNT)

        if (!e.isDropItems) return

        record[EbiPowerModule.PS_KEY_BROKEN_BLOCKS_COUNT] = brokenBlocksCount + 1

        if (brokenBlocksCount + 1 == BREAK_BLOCK_BONUS_LIMIT) {
            HintModule.achieve(e.player, Hint.MINERS_DREAM)
        }

        var ep = 1
        val tool = e.player.inventory.itemInMainHand
        val luck = tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)
        if (luck) {
            ep += when (e.block.type) {
                Material.COAL_ORE -> 1
                Material.DEEPSLATE_COAL_ORE -> 1
                Material.IRON_ORE -> 1
                Material.DEEPSLATE_IRON_ORE -> 1
                Material.COPPER_ORE -> 1
                Material.DEEPSLATE_COPPER_ORE -> 1
                Material.GOLD_ORE -> 4
                Material.DEEPSLATE_GOLD_ORE -> 4
                Material.REDSTONE_ORE -> 2
                Material.DEEPSLATE_REDSTONE_ORE -> 2
                Material.LAPIS_ORE -> 3
                Material.DEEPSLATE_LAPIS_ORE -> 3
                Material.DIAMOND_ORE -> 7
                Material.DEEPSLATE_DIAMOND_ORE -> 7
                Material.EMERALD_ORE -> 11
                Material.DEEPSLATE_EMERALD_ORE -> 11
                Material.DEEPSLATE -> 1
                Material.OBSIDIAN -> 3
                Material.NETHER_QUARTZ_ORE -> 1
                Material.NETHER_GOLD_ORE -> 1
                Material.ANCIENT_DEBRIS -> 19
                else -> 0
            }
        }

        EbiPowerModule.tryGive(e.player, ep)
        HintModule.achieve(e.player, Hint.MINERS_NEWBIE)
    }

    @EventHandler
    fun onNewDayToResetBrokenBlocksCount(e: RealTimeNewDayEvent) {
        PlayerStore.openAll().forEach {
            it[EbiPowerModule.PS_KEY_BROKEN_BLOCKS_COUNT] = 0
        }
    }

    @EventHandler
    fun onTransferPlayerData(e: TransferPlayerDataEvent) {
        val power = EbiPowerModule.get(e.from)
        EbiPowerModule.tryTake(e.from, power)
        EbiPowerModule.tryGive(e.to, power)
    }

    private fun notification(p: Player, mes: String) {
        p.sendActionBar(Component.text(mes))
        // p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
    }

    private fun playerIsInBlacklisted(p: Player): Boolean {
        // クリエイティブモードであれば、エビパワー取得対象外とする
        return p.gameMode == GameMode.CREATIVE || !WorldModule.getWorldInfo(p.world).canEarnEbiPower
    }

    private fun getMobDropBonus(stack: ItemStack): Int {
        return stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
    }

    private fun getBlockDropBonus(stack: ItemStack): Int {
        return stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
    }

    private val random = Random()
}