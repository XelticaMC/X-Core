package work.xeltica.craft.core

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import work.xeltica.craft.core.api.HookBase
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.commands.CommandCountdown
import work.xeltica.craft.core.commands.CommandLocalTime
import work.xeltica.craft.core.commands.CommandPvp
import work.xeltica.craft.core.commands.CommandReport
import work.xeltica.craft.core.commands.CommandRespawn
import work.xeltica.craft.core.commands.CommandSignEdit
import work.xeltica.craft.core.commands.CommandXDebug
import work.xeltica.craft.core.commands.CommandXReload
import work.xeltica.craft.core.commands.CommandXtp
import work.xeltica.craft.core.commands.CommandXtpReset
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.handlers.NightmareHandler
import work.xeltica.craft.core.hooks.CitizensHook
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.hooks.VaultHook
import work.xeltica.craft.core.modules.autoCrafter.AutoCrafterModule
import work.xeltica.craft.core.modules.bedrock.BedrockModule
import work.xeltica.craft.core.modules.bossbar.BossBarModule
import work.xeltica.craft.core.modules.cat.CatModule
import work.xeltica.craft.core.modules.clover.CloverModule
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.modules.eventFarm.EventFarmModule
import work.xeltica.craft.core.modules.eventFirework.EventFireworkModule
import work.xeltica.craft.core.modules.eventHalloween.EventHalloweenModule
import work.xeltica.craft.core.modules.eventSummer.EventSummerModule
import work.xeltica.craft.core.modules.fly.FlyModule
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.kusa.KusaModule
import work.xeltica.craft.core.modules.livemode.LiveModeModule
import work.xeltica.craft.core.modules.meta.MetaModule
import work.xeltica.craft.core.modules.mobball.MobBallModule
import work.xeltica.craft.core.modules.motd.MotdModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.modules.notification.NotificationModule
import work.xeltica.craft.core.modules.omikuji.OmikujiModule
import work.xeltica.craft.core.modules.payments.PaymentsModule
import work.xeltica.craft.core.modules.player.PlayerModule
import work.xeltica.craft.core.modules.promotion.PromotionModule
import work.xeltica.craft.core.modules.quickchat.QuickChatModule
import work.xeltica.craft.core.modules.ranking.RankingModule
import work.xeltica.craft.core.modules.stamprally.StampRallyModule
import work.xeltica.craft.core.modules.vehicle.VehicleModule
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.modules.xphone.XphoneModule
import work.xeltica.craft.core.runnables.DaylightObserver
import work.xeltica.craft.core.runnables.NightmareRandomEvent
import work.xeltica.craft.core.runnables.RealTimeObserver
import work.xeltica.craft.core.utils.Ticks

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Xeltica
 */
class XCorePlugin : JavaPlugin() {

    override fun onEnable() {
        instance = this
        PlayerStore.onEnable()
        Gui.onEnable()
        loadHooks()
        loadModules()

        // TODO 廃止
        loadCommands()
        loadHandlers()
        loadRunnables()

        logger.info("Booted XelticaMC Core System.")
    }

    override fun onDisable() {
        CommandRegistry.clearMap()
        PlayerStore.onDisable()
        unloadHooks()
        unloadModules()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return CommandRegistry.onCommand(sender, command, label, args)
    }

    private fun loadHooks() {
        // 連携フックの有効化
        hooks.forEach {
            try {
                it.onEnable()
                logger.info("Successfully enabled ${it.javaClass.name}!")
            } catch (e: Exception) {
                logger.severe("Failed to enable '${it.javaClass.name}'")
                e.printStackTrace()
            }
        }
    }

    private fun unloadHooks() {
        // 連携フックの有効化
        hooks.forEach {
            try {
                it.onDisable()
                logger.info("Successfully disabled ${it.javaClass.name}!")
            } catch (e: Exception) {
                logger.severe("Failed to disable '${it.javaClass.name}'")
                e.printStackTrace()
            }
        }
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

    private fun loadCommands() {
        CommandRegistry.clearMap()

        CommandRegistry.register("respawn", CommandRespawn())
        CommandRegistry.register("pvp", CommandPvp())
        CommandRegistry.register("signedit", CommandSignEdit())
        CommandRegistry.register("report", CommandReport())
        CommandRegistry.register("localtime", CommandLocalTime())
        CommandRegistry.register("xtp", CommandXtp())
        CommandRegistry.register("countdown", CommandCountdown())
        CommandRegistry.register("xreload", CommandXReload())
        CommandRegistry.register("xtpreset", CommandXtpReset())
        CommandRegistry.register("xdebug", CommandXDebug())

        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
    }

    private fun loadHandlers() {
        val pm = server.pluginManager
        pm.registerEvents(NightmareHandler(), this)
        logger.info("Loaded NightmareHandler")
    }

    private fun loadRunnables() {
        DaylightObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        NightmareRandomEvent(this).runTaskTimer(this, 0, Ticks.from(15.0).toLong())
        RealTimeObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
    }

    private val hooks: Array<HookBase> = arrayOf(
        CitizensHook,
        DiscordHook,
        VaultHook,
    )

    private val modules: Array<ModuleBase> = arrayOf(
        AutoCrafterModule,
        BedrockModule,
        BossBarModule,
        CatModule,
        CloverModule,
        CounterModule,
        EbiPowerModule,
        EventFarmModule,
        EventFireworkModule,
        EventHalloweenModule,
        EventSummerModule,
        FlyModule,
        HintModule,
        HubModule,
        ItemModule,
        KusaModule,
        LiveModeModule,
        MetaModule,
        MobBallModule,
        MotdModule,
        NbsModule,
        NotificationModule,
        OmikujiModule,
        PaymentsModule,
        PlayerModule,
        PromotionModule,
        QuickChatModule,
        RankingModule,
        StampRallyModule,
        VehicleModule,
        WorldModule,
        XphoneModule,
    )

    companion object {
        @JvmStatic
        lateinit var instance: XCorePlugin
            private set
    }
}