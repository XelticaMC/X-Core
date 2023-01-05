package work.xeltica.craft.core.modules.nightmare

import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Bee
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Piglin
import org.bukkit.entity.Player
import org.bukkit.entity.Wolf
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Ticks
import java.util.Objects
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/**
 * ナイトメアワールドを制御するモジュールです。
 */
object NightmareModule : ModuleBase() {
    const val NIGHTMARE_WORLD_NAME = "nightmare2"

    private val random = Random()

    /**
     * ナイトメアワールドでランダムに発生する現象
     */
    private val tasks = listOf<(Location, Int, Player) -> Unit>(
        this::taskSummonBlazes,
        this::taskSummonPiglins,
        this::taskSummonIllagers,
        this::taskSummonBees,
        this::taskSummonWolves,
        this::taskSummonLightning,
        this::taskSummonCreeper,
        this::taskSummonShulker,
    )

    private val illigers = arrayOf(
        EntityType.PILLAGER,
        EntityType.PILLAGER,
        EntityType.VINDICATOR,
        EntityType.VINDICATOR,
        EntityType.WITCH
    )

    private lateinit var nightmareWorld: World

    override fun onEnable() {
        val world = Bukkit.getServer().getWorld(NIGHTMARE_WORLD_NAME)
        if (world == null) {
            Bukkit.getLogger().severe("ワールド「${NIGHTMARE_WORLD_NAME}」が見つからないため、ナイトメア機能を無効化します。")
            return
        }
        nightmareWorld = world
        nightmareWorld.apply {
            difficulty = Difficulty.HARD
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.MOB_GRIEFING, false)
            setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, false)
            setGameRule(GameRule.UNIVERSAL_ANGER, true)
            time = 18000
            setStorm(true)
            weatherDuration = 20000
            isThundering = true
            thunderDuration = 20000
        }
        registerHandler(NightmareHandler())
        Bukkit.getScheduler().runTaskTimer(XCorePlugin.instance, this::run, 0, Ticks.from(15.0).toLong())
    }

    private fun run() {
        val players = nightmareWorld.players
        nightmareWorld.setStorm(true)
        nightmareWorld.isThundering = true

        players.forEach(this::logicForPlayer)
    }

    private fun logicForPlayer(player: Player) {
        val location = player.location

        // プレイヤー周辺でランダムに雷を発生させる
        summonLightning(location.clone())

        val dice = random.nextInt(tasks.size)
        val pivot = location.clone().add(Vector(random.nextInt(10) + 5, 0, random.nextInt(10) + 5))
        pivot.y = location.world.getHighestBlockYAt(pivot.blockX, pivot.blockZ).toDouble()
        val amount = random.nextInt(3) + 1

        tasks[dice](pivot, amount, player)
    }

    /**
     * 指定した位置に近い、ランダムな位置に雷を発生させます。
     */
    private fun summonLightning(location: Location) {
        val x = cos(random.nextDouble() * 2 * Math.PI)
        val z = sin(random.nextDouble() * 2 * Math.PI)
        location.add(x * 64, 0.0, z * 64)
        location.y = location.world.getHighestBlockYAt(location.blockX, location.blockZ).toDouble()

        location.world.strikeLightning(location)
    }

    private fun taskSummonBlazes(pivot: Location, amount: Int, player: Player) {
        for (i in 0 until amount) {
            val loc = pivot.clone().add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = (pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ) + 1).toDouble()
            nightmareWorld.spawnEntity(loc, EntityType.BLAZE)
        }
    }

    private fun taskSummonPiglins(pivot: Location, amount: Int, player: Player) {
        for (i in 0 until amount) {
            val loc = pivot.clone().add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = (pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ) + 1).toDouble()
            val piglin = nightmareWorld.spawnEntity(loc, EntityType.PIGLIN) as Piglin

            // ゾンビ化しない
            piglin.isImmuneToZombification = true

            // 50:50で武器が決まる
            val equip = ItemStack(if (random.nextBoolean()) Material.CROSSBOW else Material.GOLDEN_SWORD)
            Objects.requireNonNull(piglin.equipment).setItemInMainHand(equip)

            // 5%の確率でスピードバフ
            if (random.nextInt(100) < 5) {
                piglin.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 30000, 2))
            }

            // 5%の確率で子供
            if (random.nextInt(100) < 5) {
                piglin.setBaby()
            }
        }
    }

    private fun taskSummonIllagers(pivot: Location, amount: Int, player: Player) {
        for (i in 0 until amount) {
            val loc = pivot.clone().add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = (pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ) + 1).toDouble()
            nightmareWorld.spawnEntity(loc, illigers[random.nextInt(illigers.size)])
        }
    }

    private fun taskSummonBees(pivot: Location, amount: Int, player: Player) {
        pivot.add(0.0, 5.0, 0.0)
        for (i in 0 until amount) {
            val loc = pivot.clone().add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ + 5).toDouble()
            val bee = nightmareWorld.spawnEntity(loc, EntityType.BEE) as Bee
            // 30分おいかり
            bee.anger = Ticks.from(30, 0.0)
            bee.target = player
        }
    }

    private fun taskSummonWolves(pivot: Location, amount: Int, player: Player) {
        for (i in 0 until amount) {
            val loc = pivot.clone().add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = (pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ) + 1).toDouble()
            val wolf = nightmareWorld.spawnEntity(loc, EntityType.WOLF) as Wolf
            // 30分おいかり
            wolf.isAngry = true
            wolf.target = player
        }
    }

    private fun taskSummonLightning(pivot: Location, amount: Int, player: Player) {
        val instance = XCorePlugin.instance
        val scheduler = Bukkit.getScheduler()
        val strike = Runnable { pivot.world.strikeLightning(pivot) }
        // 今いる場所に、5分後に雷を3つ落とす
        scheduler.runTaskLater(instance, Runnable {
            scheduler.runTask(instance, strike)
            scheduler.runTaskLater(instance, strike, Ticks.from(0.5).toLong())
            scheduler.runTaskLater(instance, strike, Ticks.from(1.0).toLong())
        }, Ticks.from(6, 0.0).toLong())
    }

    private fun taskSummonCreeper(pivot: Location, amount: Int, player: Player) {
        val creeper = pivot.world.spawnEntity(pivot, EntityType.CREEPER) as Creeper
        val scheduler = Bukkit.getScheduler()
        creeper.isPowered = true
        scheduler.runTaskLater(XCorePlugin.instance, Runnable { creeper.ignite() }, Ticks.from(5, 0.0).toLong())
    }

    private fun taskSummonShulker(pivot: Location, amount: Int, player: Player) {
        for (i in 0 until amount) {
            val loc = pivot.add(Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2))
            loc.y = (pivot.world.getHighestBlockYAt(loc.blockX, loc.blockZ) + 1).toDouble()
            nightmareWorld.spawnEntity(loc, if (random.nextInt(100) > 80) EntityType.SHULKER else EntityType.ENDERMAN)
        }
    }
}