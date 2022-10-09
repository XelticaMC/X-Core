package work.xeltica.craft.core.modules.halloween

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Biome
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.models.CandyStoreItem
import work.xeltica.craft.core.utils.Config
import work.xeltica.craft.core.utils.Ticks
import java.io.IOException
import java.util.*

object HalloweenModule : ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(CandyStoreItem::class.java, "CandyStoreItem")
        registerHandler(HalloweenHandler())
        items.clear()

        // アメストアの賞品を読み込む
        candyStoreConfig = Config("ep") {
            items.addAll(it.conf.getList("item", ArrayList<CandyStoreItem>()) as MutableList<CandyStoreItem>)
        }

        registerCommand("candystore", CandyStoreCommand())
    }

    /**
     * モブをイベント用モブに置き換えて生成します。
     */
    fun replaceMob(entity: Monster) {
        val world = entity.world
        val biome = world.getBiome(entity.location).key
        val isHuskBiome = huskBiomes.contains(biome)
        val isStrayBiome = strayBiomes.contains(biome)
        var entityType = entityTypes[random.nextInt(entityTypes.size)]
        if (entityType == EntityType.ZOMBIE && isHuskBiome) entityType = EntityType.HUSK
        if (entityType == EntityType.SKELETON && isStrayBiome) entityType = EntityType.STRAY
        val location = entity.location
        entity.remove()
        // Y64以下ではスポーンなし
        if (location.y <= 64) {
            return
        }
        val l = Location(world, 0.0, 0.0, 0.0)
        // レールが近くにあったらスポーンなし
        for (z in (location.z - 8).toInt()..(location.z + 8).toInt()) {
            for (y in (location.y - 8).toInt()..(location.y + 8).toInt()) {
                for (x in (location.x - 8).toInt()..(location.x + 8).toInt()) {
                    l.set(x.toDouble(), y.toDouble(), z.toDouble())
                    if (Tag.RAILS.isTagged(l.block.type)) {
                        return
                    }
                }
            }
        }
        world.spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (it is PigZombie) {
                // ゾンビピグリンの場合は中立だと面白くないので怒らせる
                it.anger = Ticks.from(15, 0.0)
            }
            if (it is Monster) {
                with(it.equipment) {
                    helmet = ItemStack(if (random.nextInt(100) < 10) org.bukkit.Material.JACK_O_LANTERN else org.bukkit.Material.CARVED_PUMPKIN)
                    helmetDropChance = 0f
                    chestplate = null
                    leggings = null
                    boots = null
                    setItemInMainHand(null)
                    setItemInOffHand(null)
                }
                it.canPickupItems = false
                it.setMetadata(eventMobMetaDataKey, FixedMetadataValue(XCorePlugin.instance, true))
            }
        }
    }

    fun replaceDrops(drops: MutableList<ItemStack>, killer: Player) {
        val amount = 1 + random.nextInt(killer.equipment.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1)
        drops.clear()
        drops.add(generateCandy(amount))
        // 5%の確率でクッキー
        if (random.nextInt(100) < 5) {
            drops.add(ItemStack(Material.COOKIE))
        }
        // 1%の確率でダイヤモンド
        if (random.nextInt(100) < 1) {
            drops.add(ItemStack(Material.DIAMOND))
        }
        // 1%の確率でエメラルド
        if (random.nextInt(100) < 1) {
            drops.add(ItemStack(Material.EMERALD))
        }
    }

    fun addItem(item: CandyStoreItem) {
        items.add(item)
        candyStoreConfig.conf["items"] = items
        try {
            candyStoreConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteItem(item: CandyStoreItem) {
        items.remove(item)
        candyStoreConfig.conf["items"] = items
        try {
            candyStoreConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun openCandyStore(player: Player) {
        val menuItems = items.map {
            val name = it.item.displayName()
                .style(Style.empty())
                .append(Component.text(" (${it.cost}アメ)"))
            // MenuItem(name, {  }, it.item)
        }
    }

    /**
     * イベントモブかどうかを検証します
     */
    fun Entity.isEventMob() = hasMetadata(eventMobMetaDataKey)

    /**
     * アイテムがアメかどうかを検証します
     */
    fun ItemStack.isCandy(): Boolean {
        val lore = itemMeta.lore()
        return lore != null && PlainTextComponentSerializer.plainText().serialize(lore[0]) == loreString
    }

    /**
     * アメを生成します
     */
    fun generateCandy(amount: Int = 1): ItemStack {
        val itemStack = ItemStack(Material.HEART_OF_THE_SEA, amount)
        itemStack.editMeta {
            it.displayName(Component.text("アメ"))
            it.lore(listOf(
                Component.text(loreString)
            ))
        }
        return itemStack
    }

    // TODO: ハードコーディングしなくても良いよう、ハスクとストレイが湧くバイオームが取れないだろうか…
    private val huskBiomes = listOf(
        Biome.DESERT.key,
    )

    private val strayBiomes = listOf(
        Biome.SNOWY_PLAINS.key,
        Biome.ICE_SPIKES.key,
        Biome.FROZEN_RIVER.key,
    )

    private val entityTypes = listOf(
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.WITHER_SKELETON,
    )

    private val random = Random()
    private val eventMobMetaDataKey = "halloween${GregorianCalendar().get(Calendar.YEAR)}"
    private val loreString = "XelticaMC${GregorianCalendar().get(Calendar.YEAR)}ハロウィン"

    private val items = mutableListOf<CandyStoreItem>()
    private lateinit var candyStoreConfig: Config
}