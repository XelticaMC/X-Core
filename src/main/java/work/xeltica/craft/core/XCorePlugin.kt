package work.xeltica.craft.core

import java.io.IOException
import java.util.Random
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import net.luckperms.api.LuckPerms

import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.plugins.CitizenTimerCalculator
import work.xeltica.craft.core.api.Ticks
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.workers.*
import work.xeltica.craft.core.modules.*
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.utils.EventUtility

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Xeltica
 */
class XCorePlugin : JavaPlugin() {

    override fun onEnable() {
        instance = this
        loadPlugins()
        loadWorkers()

        // モジュール初期化
        for (mod in modules) {
            try {
                mod.onEnable()
                logger.info("${mod.javaClass.name}を有効化しました")
            } catch (e: Exception) {
                logger.severe("${mod.javaClass.name} の有効化処理内で予期しないエラーが発生")
                e.printStackTrace()
            }
        }

        // モジュール初期化後処理（モジュール間連携の初期化など）
        for (mod in modules) {
            try {
                mod.onEnable()
                logger.info("${mod.javaClass.name}の初期化後処理が完了しました")
            } catch (e: Exception) {
                logger.severe("${mod.javaClass.name} の初期化後処理内で予期しないエラーが発生")
                e.printStackTrace()
            }
        }

        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
        logger.info("X-Core 準備完了。")
    }

    override fun onDisable() {
        CommandRegistry.clearMap()
        Gui.resetInstance()
        unloadPlugins()
        modules.forEach {
            it.onDisable()
        }
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        if (provider != null) {
            val luckPerms = provider.provider
            luckPerms.contextManager.unregisterCalculator(calculator)
        }
        logger.info("X-Core を停止しました。")
    }

    private fun loadWorkers() {
        DaylightObserveWorker(this).runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        NightmareControlWorker(this).runTaskTimer(this, 0, Ticks.from(15.0).toLong())
        FlyingObserveWorker().runTaskTimer(this, 0, 4)
        RealTimeObserveWorker().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        EbipowerObserveWorker().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        val tick = 10
        object : BukkitRunnable() {
            override fun run() {
                VehicleModule.tick(tick)
                PlayerStoreModule.openAll().forEach {
                    // オフラインなら処理しない
                    if (Bukkit.getPlayer(it.playerId) == null) return
                    var time = it.getInt(PlayerDataKey.NEWCOMER_TIME, 0)
                    time -= tick
                    if (time <= 0) {
                        it.delete(PlayerDataKey.NEWCOMER_TIME)
                    } else {
                        it[PlayerDataKey.NEWCOMER_TIME] = time
                    }
                }
                try {
                    PlayerStoreModule.save()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.runTaskTimer(this, 0, tick.toLong())
        if (EventUtility.isEventNow()) {
            object : BukkitRunnable() {
                private val colors = arrayOf(
                    Color.AQUA,
                    Color.BLUE,
                    Color.FUCHSIA,
                    Color.GREEN,
                    Color.LIME,
                    Color.MAROON,
                    Color.NAVY,
                    Color.OLIVE,
                    Color.ORANGE,
                    Color.PURPLE,
                    Color.RED,
                    Color.SILVER,
                    Color.TEAL,
                    Color.WHITE,
                    Color.YELLOW
                )

                private val types = arrayOf(
                    FireworkEffect.Type.BALL,
                    FireworkEffect.Type.STAR,
                    FireworkEffect.Type.BURST,
                    FireworkEffect.Type.BALL_LARGE,
                )

                override fun run() {
                    Bukkit.getOnlinePlayers()
                        .filter { it.world.name == "main" }
                        .forEach {
                            for (i in 1..random.nextInt(5)) {
                                val sky = it.location.clone()
                                sky.y += random.nextDouble(10.0, 30.0)
                                sky.x += random.nextDouble(-48.0, 48.0)
                                sky.z += random.nextDouble(-48.0, 48.0)
                                val effect = FireworkEffect.builder()
                                    .with(types[random.nextInt(types.size)])
                                    .withColor(colors[random.nextInt(colors.size)])
                                    .build()
                                sky.world.spawnEntity(sky, EntityType.FIREWORK, SpawnReason.CUSTOM) { theEntity ->
                                    if (theEntity is Firework) {
                                        val meta = theEntity.fireworkMeta
                                        // すぐに爆発させる
                                        meta.power = 0
                                        meta.addEffect(effect)
                                        theEntity.fireworkMeta = meta
                                    }
                                }
                            }
                        }
                }
            }.runTaskTimer(this, Ticks.from(3.0).toLong(), Ticks.from(10.0).toLong())
        }
    }

    private fun loadPlugins() {
        VaultPlugin.getInstance().onEnable(this)
        calculator = CitizenTimerCalculator()
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        if (luckPerms == null) {
            logger.severe("X-CoreはLuckPermsを必要とします。X-Coreを終了します。")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        luckPerms.contextManager.registerCalculator(calculator)
    }

    private fun unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this)
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        luckPerms?.contextManager?.unregisterCalculator(calculator)
    }

    private val modules = listOf(
        BedrockDisclaimerModule,
        BossBarModule,
        CloverModule,
        CustomItemModule,
        DiscordModule,
        EbipowerModule,
        HintModule,
        HubModule,
        MetaModule,
        MobBallModule,
        MobDroppingEpModule,
        NotificationModule,
        OmikujiModule,
        PlayerStoreModule,
        XphoneModule,
    )

    private lateinit var calculator: CitizenTimerCalculator
    private val random = Random()

    companion object {
        @JvmStatic
        lateinit var instance: XCorePlugin
            private set
    }
}