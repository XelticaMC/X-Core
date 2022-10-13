package work.xeltica.craft.core.modules.halloween

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.utils.Config
import work.xeltica.craft.core.utils.Ticks
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import java.util.function.Consumer

object HalloweenModule : ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(CandyStoreItem::class.java, "CandyStoreItem")
        items.clear()
        // アメストアの賞品を読み込む
        candyStoreConfig = Config(CONFIG_NAME) {
            items.addAll(it.conf.getList(CONFIG_KEY_ITEMS, ArrayList<CandyStoreItem>()) as MutableList<CandyStoreItem>)
            _isEventMode = it.conf.getBoolean(CONFIG_KEY_EVENT_MODE)
        }

        registerHandler(HalloweenHandler())
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
                it.setMetadata(METADATA_KEY_EVENT_MOB, FixedMetadataValue(XCorePlugin.instance, true))
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
        candyStoreConfig.conf[CONFIG_KEY_ITEMS] = items
        try {
            candyStoreConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteItem(item: CandyStoreItem) {
        items.remove(item)
        candyStoreConfig.conf[CONFIG_KEY_ITEMS] = items
        try {
            candyStoreConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun openCandyStore(player: Player) {
        openCandyStoreUI(player, "アメストア") {
            if (it.cost > 0 && !tryTakeCandy(player, it.cost)) {
                // アメの徴収に失敗した
                Gui.getInstance().error(player, "アメが足りません。")
            } else {
                // アイテムを渡す
                when (it) {
                    is CandyStoreItem -> {
                        player.world.dropItem(player.location, it.item.clone())
                    }
                    is CandyStoreEPItem -> {
                        EbiPowerStore.getInstance().tryGive(player, it.ep)
                    }
                }
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
                player.sendMessage("${ChatColor.AQUA}引き換えました！")
            }

            object : BukkitRunnable() {
                override fun run() {
                    openCandyStore(player)
                }
            }.runTask(XCorePlugin.instance)
        }
    }

    fun openCandyStoreUI(player: Player, title: String, onChosen: Consumer<ICandyStoreItem>) {
        val menuItems = items.map {
            val item: ItemStack = it.item
            val name: String = getItemName(item) ?: ""
            val displayName = name + "×" + item.amount + " (" + it.cost + "アメ)"
            MenuItem(displayName, {_ ->
                onChosen.accept(it)
            }, it.item.clone())
        }.toTypedArray()
        Gui.getInstance().openMenu(player, title, listOf(*menuItems, MenuItem("2エビパワー (1アメ)", {
            onChosen.accept(CandyStoreEPItem(2, 1))
        }, Material.EMERALD, null, true)))
    }

    /**
     * イベントモブかどうかを検証します
     */
    fun Entity.isEventMob() = hasMetadata(METADATA_KEY_EVENT_MOB)

    /**
     * アイテムがアメかどうかを検証します
     */
    fun ItemStack.isCandy(): Boolean {
        val lore = itemMeta.lore()
        return lore != null && PlainTextComponentSerializer.plainText().serialize(lore[0]) == ITEM_LORE_STRING
    }

    /**
     * アメを生成します
     */
    fun generateCandy(amount: Int = 1): ItemStack {
        val itemStack = ItemStack(Material.HEART_OF_THE_SEA, amount)
        itemStack.editMeta {
            it.displayName(Component.text("アメ"))
            it.lore(listOf(
                Component.text(ITEM_LORE_STRING)
            ))
        }
        return itemStack
    }

    fun tryTakeCandy(player: Player, amount: Int): Boolean {
        if (amount == 0) return true
        if (amount < 0) throw IllegalArgumentException()
        val balance = player.inventory
            .filter { it !== null && it.isCandy() }
            .sumOf { it.amount }
        if (balance < amount) {
            return false
        }
        val candy = generateCandy(amount)
        player.inventory.removeItemAnySlot(candy)
        return true
    }

    var isEventMode
        get() = _isEventMode
        set(value) {
            _isEventMode = value

            candyStoreConfig.conf["eventMode"] = value
            try {
                candyStoreConfig.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    /**
     * 指定したアイテムスタックから名前を取得します。
     * @param item 取得するアイテム
     * @return アイテムのdisplayName
     */
    private fun getItemName(item: ItemStack): String? {
        val dn = item.itemMeta.displayName()
        return if (dn != null) PlainTextComponentSerializer.plainText().serialize(dn) else item.i18NDisplayName
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

    private val METADATA_KEY_EVENT_MOB = "halloween${GregorianCalendar().get(Calendar.YEAR)}"
    private val ITEM_LORE_STRING = "XelticaMC${GregorianCalendar().get(Calendar.YEAR)}ハロウィン"
    private val CONFIG_NAME = "candyStore"
    private val CONFIG_KEY_ITEMS = "items"
    private val CONFIG_KEY_EVENT_MODE = "eventMode"

    private val random = Random()

    private val items = mutableListOf<CandyStoreItem>()
    private var _isEventMode = false
    private lateinit var candyStoreConfig: Config
}