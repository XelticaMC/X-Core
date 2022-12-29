package work.xeltica.craft.core

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.utils.Ticks
import work.xeltica.craft.core.commands.*
import work.xeltica.craft.core.stores.*
import work.xeltica.craft.core.handlers.*
import work.xeltica.craft.core.runnables.*
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.modules.xphone.XphoneModule
import work.xeltica.craft.core.modules.quickchat.QuickChatModule
import work.xeltica.craft.core.modules.notification.NotificationModule
import work.xeltica.craft.core.modules.bossbar.BossBarModule
import work.xeltica.craft.core.modules.clover.CloverModule
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.modules.eventFarm.EventFarmModule
import work.xeltica.craft.core.modules.eventFirework.EventFireworkModule
import work.xeltica.craft.core.modules.eventHalloween.EventHalloweenModule
import work.xeltica.craft.core.modules.payments.PaymentsModule
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.meta.MetaModule
import work.xeltica.craft.core.modules.mobball.MobBallModule
import work.xeltica.craft.core.modules.mobep.MobEPModule
import work.xeltica.craft.core.modules.ranking.RankingModule
import work.xeltica.craft.core.modules.stamprally.StampRallyModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.modules.omikuji.OmikujiModule
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.player.PlayerModule
import work.xeltica.craft.core.modules.promotion.CommandPromo
import work.xeltica.craft.core.modules.promotion.PromotionModule
import work.xeltica.craft.core.modules.vehicle.VehicleModule
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.utils.DiscordService

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Xeltica
 */
class XCorePlugin : JavaPlugin() {

    override fun onEnable() {
        instance = this
        DiscordService()
        loadModules()

        // TODO 廃止
        loadPlugins()
        loadStores()
        loadCommands()
        loadHandlers()
        DaylightObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        FlyingObserver().runTaskTimer(this, 0, 4)
        NightmareRandomEvent(this).runTaskTimer(this, 0, Ticks.from(15.0).toLong())
        RealTimeObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        TimeAttackObserver().runTaskTimer(this, 0, 5)

        val tick = Ticks.from(1.0)
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach {
                    val record = PlayerModule.open(it)
                    var time = record.getInt(PlayerDataKey.NEWCOMER_TIME, 0)
                    time -= tick
                    if (time <= 0) {
                        record.delete(PlayerDataKey.NEWCOMER_TIME)
                    } else {
                        record[PlayerDataKey.NEWCOMER_TIME] = time
                    }
                }
            }
        }.runTaskTimer(this, 0, tick.toLong())

        logger.info("Booted XelticaMC Core System.")
    }

    override fun onDisable() {
        CommandRegistry.clearMap()
        Gui.resetInstance()
        unloadPlugins()
        unloadModules()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return CommandRegistry.onCommand(sender, command, label, args)
    }

    private fun loadModules() {
        // モジュールの有効化フック
        modules.forEach {
            try {
                it.onEnable()
                logger.info("Successfully enabled ${it.javaClass.name}!")
            } catch (e: Exception) {
                logger.severe("Failed to enable '${it.javaClass.name}'")
                e.printStackTrace()
            }
        }
        // モジュールの有効化後処理フック（各モジュールの連携とか）
        modules.forEach {
            try {
                it.onPostEnable()
                logger.info("Successfully post-enabled ${it.javaClass.name}!")
            } catch (e: Exception) {
                logger.severe("Failed to post-enable '${it.javaClass.name}'")
                e.printStackTrace()
            }
        }
    }

    private fun unloadModules() {
        // モジュールの無効化フック
        modules.forEach {
            try {
                it.onDisable()
                logger.info("Successfully disabled ${it.javaClass.name}!")
            } catch (e: Exception) {
                logger.severe("Failed to disable '${it.javaClass.name}'")
                e.printStackTrace()
            }
        }
    }

    private fun loadStores() {
        NickNameStore()
    }

    private fun loadCommands() {
        CommandRegistry.clearMap()

        CommandRegistry.register("respawn", CommandRespawn())
        CommandRegistry.register("pvp", CommandPvp())
        CommandRegistry.register("signedit", CommandSignEdit())
        CommandRegistry.register("report", CommandReport())
        CommandRegistry.register("localtime", CommandLocalTime())
        CommandRegistry.register("promo", CommandPromo())
        CommandRegistry.register("cat", CommandCat())
        CommandRegistry.register("xtp", CommandXtp())
        CommandRegistry.register("__core_gui_event__", CommandXCoreGuiEvent())
        CommandRegistry.register("live", CommandLive())
        CommandRegistry.register("nick", CommandNickName())
        CommandRegistry.register("countdown", CommandCountdown())
        CommandRegistry.register("xreload", CommandXReload())
        CommandRegistry.register("xtpreset", CommandXtpReset())
        CommandRegistry.register("xdebug", CommandXDebug())

        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
    }

    private fun loadHandlers() {
        val pm = server.pluginManager
        pm.registerEvents(WorldHandler(), this)
        logger.info("Loaded WorldHandler")
        pm.registerEvents(NightmareHandler(), this)
        logger.info("Loaded NightmareHandler")
        pm.registerEvents(LiveModeHandler(), this)
        logger.info("Loaded LiveModeHandler")
        pm.registerEvents(PlayerTntHandler(), this)
        logger.info("Loaded PlayTntHandler")
        pm.registerEvents(MiscHandler(), this)
        logger.info("Loaded MiscHandler")
        pm.registerEvents(LoginBonusHandler(), this)
        logger.info("Loaded LoginBonusHandler")
        pm.registerEvents(TicketWildareaBHandler(), this)
        logger.info("Loaded TicketWildareaBHandler")
        pm.registerEvents(Gui.getInstance(), this)
        logger.info("Loaded Gui")
    }

    private fun loadPlugins() {
        VaultPlugin.getInstance().onEnable(this)
    }

    private fun unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this)
    }

    private val modules: Array<ModuleBase> = arrayOf(
        BossBarModule,
        CloverModule,
        CounterModule,
        EbiPowerModule,
        EventFarmModule,
        EventFireworkModule,
        EventHalloweenModule,
        HintModule,
        HubModule,
        ItemModule,
        MetaModule,
        MobBallModule,
        MobEPModule,
        NbsModule,
        NotificationModule,
        OmikujiModule,
        PaymentsModule,
        PlayerModule,
        QuickChatModule,
        RankingModule,
        StampRallyModule,
        VehicleModule,
        WorldModule,
        XphoneModule,
        PromotionModule,
    )

    companion object {
        @JvmStatic
        lateinit var instance: XCorePlugin
            private set
    }
}