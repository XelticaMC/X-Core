package work.xeltica.craft.core

import net.kyori.adventure.text.Component
import work.xeltica.craft.core.xphone.XphoneOs.onEnabled
import work.xeltica.craft.core.xphone.XphoneOs.onDisabled
import work.xeltica.craft.core.utils.DiscordService
import work.xeltica.craft.core.runnables.DaylightObserver
import work.xeltica.craft.core.runnables.NightmareRandomEvent
import work.xeltica.craft.core.runnables.FlyingObserver
import work.xeltica.craft.core.runnables.RealTimeObserver
import work.xeltica.craft.core.runnables.EbipowerObserver
import work.xeltica.craft.core.stores.VehicleStore
import work.xeltica.craft.core.stores.PlayerStore
import work.xeltica.craft.core.models.PlayerDataKey
import java.io.IOException
import work.xeltica.craft.core.plugins.CitizenTimerCalculator
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.stores.MetaStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.stores.NbsStore
import java.util.Locale
import work.xeltica.craft.core.commands.CommandBase
import work.xeltica.craft.core.stores.OmikujiStore
import work.xeltica.craft.core.stores.HubStore
import work.xeltica.craft.core.stores.WorldStore
import work.xeltica.craft.core.stores.ItemStore
import work.xeltica.craft.core.stores.CloverStore
import work.xeltica.craft.core.stores.EbiPowerStore
import work.xeltica.craft.core.stores.HintStore
import work.xeltica.craft.core.stores.BossBarStore
import work.xeltica.craft.core.stores.NickNameStore
import work.xeltica.craft.core.stores.CounterStore
import work.xeltica.craft.core.stores.RankingStore
import work.xeltica.craft.core.stores.QuickChatStore
import work.xeltica.craft.core.stores.MobEPStore
import work.xeltica.craft.core.stores.MobBallStore
import work.xeltica.craft.core.commands.CommandOmikuji
import work.xeltica.craft.core.commands.CommandRespawn
import work.xeltica.craft.core.commands.CommandPvp
import work.xeltica.craft.core.commands.CommandSignEdit
import work.xeltica.craft.core.commands.CommandGiveCustomItem
import work.xeltica.craft.core.commands.CommandGiveMobBall
import work.xeltica.craft.core.commands.CommandReport
import work.xeltica.craft.core.commands.CommandLocalTime
import work.xeltica.craft.core.commands.CommandBoat
import work.xeltica.craft.core.commands.CommandCart
import work.xeltica.craft.core.commands.CommandPromo
import work.xeltica.craft.core.commands.CommandCat
import work.xeltica.craft.core.commands.CommandHub
import work.xeltica.craft.core.commands.CommandXtp
import work.xeltica.craft.core.commands.CommandEpShop
import work.xeltica.craft.core.commands.CommandHint
import work.xeltica.craft.core.commands.CommandXCoreGuiEvent
import work.xeltica.craft.core.commands.CommandXPhone
import work.xeltica.craft.core.commands.CommandLive
import work.xeltica.craft.core.commands.CommandNickName
import work.xeltica.craft.core.commands.CommandCounter
import work.xeltica.craft.core.commands.CommandRanking
import work.xeltica.craft.core.commands.CommandCountdown
import work.xeltica.craft.core.commands.CommandQuickChat
import work.xeltica.craft.core.commands.CommandEpEffectShop
import work.xeltica.craft.core.commands.CommandXReload
import work.xeltica.craft.core.commands.CommandXtpReset
import work.xeltica.craft.core.handlers.NewMorningHandler
import work.xeltica.craft.core.handlers.PlayerHandler
import work.xeltica.craft.core.handlers.VehicleHandler
import work.xeltica.craft.core.handlers.WakabaHandler
import work.xeltica.craft.core.handlers.HubHandler
import work.xeltica.craft.core.handlers.WorldHandler
import work.xeltica.craft.core.handlers.NightmareHandler
import work.xeltica.craft.core.handlers.XphoneHandler
import work.xeltica.craft.core.handlers.EbiPowerHandler
import work.xeltica.craft.core.handlers.LiveModeHandler
import work.xeltica.craft.core.handlers.CounterHandler
import work.xeltica.craft.core.handlers.NbsHandler
import work.xeltica.craft.core.handlers.PlayerTntHandler
import work.xeltica.craft.core.handlers.MiscHandler
import work.xeltica.craft.core.handlers.LoginBonusHandler
import work.xeltica.craft.core.handlers.TicketWildareaBHandler
import work.xeltica.craft.core.handlers.MobBallHandler
import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.utils.Ticks
import java.util.HashMap

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Xeltica
 */
class XCorePlugin : JavaPlugin() {
    override fun onEnable() {
        instance = this
        loadPlugins()
        loadStores()
        loadCommands()
        loadHandlers()
        DiscordService()
        onEnabled()
        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
        DaylightObserver(this).runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        NightmareRandomEvent(this).runTaskTimer(this, 0, Ticks.from(15.0).toLong())
        FlyingObserver().runTaskTimer(this, 0, 4)
        RealTimeObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        EbipowerObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        val tick = 10
        object : BukkitRunnable() {
            override fun run() {
                VehicleStore.getInstance().tick(tick)
                val store = PlayerStore.getInstance()
                store.openAll().forEach {
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
                    store.save()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.runTaskTimer(this, 0, tick.toLong())
        calculator = CitizenTimerCalculator()
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        if (provider == null) {
            logger.severe("X-CoreはLuckPermsを必要とします。X-Coreを終了します。")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        val luckPerms = provider.provider
        luckPerms.contextManager.registerCalculator(calculator!!)
        val meta = MetaStore.getInstance()
        if (meta.isUpdated) {
            var prev = meta.previousVersion
            if (prev == null) prev = "unknown"
            val current = meta.currentVersion
            val text = String.format("§aコアシステムを更新しました。%s -> %s", prev, current)
            if (meta.postToDiscord) {
                DiscordService.getInstance().postChangelog(current, meta.changeLog)
            }
            Bukkit.getServer()
                .audiences()
                .forEach {
                    it!!.sendMessage(Component.text(text))
                    for (log in meta.changeLog) {
                        it.sendMessage(Component.text("・$log"))
                    }
                }
        }
        logger.info("Booted XelticaMC Core System.")
    }

    override fun onDisable() {
        commands.clear()
        Gui.resetInstance()
        unloadPlugins()
        NbsStore.getInstance().stopAll()
        onDisabled()
        val provider = Bukkit.getServicesManager().getRegistration(
            LuckPerms::class.java
        )
        if (provider != null) {
            val luckPerms = provider.provider
            luckPerms.contextManager.unregisterCalculator(calculator!!)
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val name = command.name.lowercase(Locale.getDefault())
        val com = commands[name] ?: return false
        return com.execute(sender, command, label, args)
    }

    private fun loadStores() {
        OmikujiStore()
        VehicleStore()
        PlayerStore()
        HubStore()
        WorldStore()
        ItemStore()
        CloverStore()
        EbiPowerStore()
        HintStore()
        MetaStore()
        BossBarStore()
        NickNameStore()
        CounterStore()
        RankingStore()
        NbsStore()
        QuickChatStore()
        MobEPStore()
        MobBallStore()
    }

    private fun loadCommands() {
        commands.clear()
        addCommand("omikuji", CommandOmikuji())
        addCommand("respawn", CommandRespawn())
        addCommand("pvp", CommandPvp())
        addCommand("signedit", CommandSignEdit())
        addCommand("givecustomitem", CommandGiveCustomItem())
        addCommand("givemobball", CommandGiveMobBall())
        addCommand("report", CommandReport())
        addCommand("localtime", CommandLocalTime())
        addCommand("boat", CommandBoat())
        addCommand("cart", CommandCart())
        addCommand("promo", CommandPromo())
        addCommand("cat", CommandCat())
        addCommand("hub", CommandHub())
        addCommand("xtp", CommandXtp())
        addCommand("epshop", CommandEpShop())
        addCommand("hint", CommandHint())
        addCommand("__core_gui_event__", CommandXCoreGuiEvent())
        addCommand("xphone", CommandXPhone())
        addCommand("live", CommandLive())
        addCommand("nick", CommandNickName())
        addCommand("counter", CommandCounter())
        addCommand("ranking", CommandRanking())
        addCommand("countdown", CommandCountdown())
        addCommand("qchat", CommandQuickChat())
        addCommand("epeffectshop", CommandEpEffectShop())
        addCommand("xreload", CommandXReload())
        addCommand("xtpreset", CommandXtpReset())
        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
    }

    private fun loadHandlers() {
        val pm = server.pluginManager
        pm.registerEvents(NewMorningHandler(), this)
        logger.info("Loaded NewMorningHandler")
        pm.registerEvents(PlayerHandler(this), this)
        logger.info("Loaded PlayerHandler")
        pm.registerEvents(VehicleHandler(), this)
        logger.info("Loaded VehicleHandler")
        pm.registerEvents(WakabaHandler(), this)
        logger.info("Loaded WakabaHandler")
        pm.registerEvents(HubHandler(), this)
        logger.info("Loaded HubHandler")
        pm.registerEvents(WorldHandler(), this)
        logger.info("Loaded WorldHandler")
        pm.registerEvents(NightmareHandler(), this)
        logger.info("Loaded NightmareHandler")
        pm.registerEvents(XphoneHandler(), this)
        logger.info("Loaded XphoneHandler")
        pm.registerEvents(EbiPowerHandler(), this)
        logger.info("Loaded EbiPowerHandler")
        pm.registerEvents(LiveModeHandler(), this)
        logger.info("Loaded LiveModeHandler")
        pm.registerEvents(CounterHandler(), this)
        logger.info("Loaded CounterHandler")
        pm.registerEvents(NbsHandler(), this)
        logger.info("Loaded NbsHandler")
        pm.registerEvents(PlayerTntHandler(), this)
        logger.info("Loaded PlayTntHandler")
        pm.registerEvents(MiscHandler(), this)
        logger.info("Loaded MiscHandler")
        pm.registerEvents(LoginBonusHandler(), this)
        logger.info("Loaded LoginBonusHandler")
        pm.registerEvents(TicketWildareaBHandler(), this)
        logger.info("Loaded TicketWildareaBHandler")
        pm.registerEvents(MobBallHandler(), this)
        logger.info("Loaded MobBallHandler")
        pm.registerEvents(Gui.getInstance(), this)
        logger.info("Loaded Gui")
    }

    private fun loadPlugins() {
        VaultPlugin.getInstance().onEnable(this)
    }

    private fun unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this)
    }

    /**
     * コマンドをコアシステムに登録します。
     * @param commandName コマンド名
     * @param command コマンドのインスタンス
     */
    private fun addCommand(commandName: String, command: CommandBase) {
        commands[commandName] = command
        val cmd = getCommand(commandName)
        if (cmd == null) {
            logger.warning("Command $commandName is not defined at the plugin.yml")
            return
        }
        cmd.tabCompleter = command
        logger.info("Command $commandName is registered")
    }

    private val commands = HashMap<String, CommandBase>()
    private var calculator: CitizenTimerCalculator? = null

    companion object {
        @JvmStatic
        lateinit var instance: XCorePlugin
            private set
    }
}