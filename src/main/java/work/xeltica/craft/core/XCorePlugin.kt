package work.xeltica.craft.core

import net.kyori.adventure.text.Component
import work.xeltica.craft.core.plugins.CitizenTimerCalculator
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.Locale
import java.util.HashMap

import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.utils.Ticks
import work.xeltica.craft.core.commands.*
import work.xeltica.craft.core.stores.*
import work.xeltica.craft.core.handlers.*
import work.xeltica.craft.core.runnables.*
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.xphone.XphoneOs
import work.xeltica.craft.core.utils.DiscordService
import work.xeltica.craft.core.models.PlayerDataKey
import work.xeltica.craft.core.utils.EventUtility
import java.util.Random

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
        XphoneOs.onEnabled()
        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
        DaylightObserver(this).runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        NightmareRandomEvent(this).runTaskTimer(this, 0, Ticks.from(15.0).toLong())
        FlyingObserver().runTaskTimer(this, 0, 4)
        RealTimeObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        EbipowerObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())
        TimeAttackObserver().runTaskTimer(this, 0, 5)
        val tick = Ticks.from(1.0)
        object : BukkitRunnable() {
            override fun run() {
                VehicleStore.getInstance().tick(tick)
                Bukkit.getOnlinePlayers().forEach {
                    val record = PlayerStore.getInstance().open(it)
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
        calculator = CitizenTimerCalculator()
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        if (luckPerms == null) {
            logger.severe("X-CoreはLuckPermsを必要とします。X-Coreを終了します。")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
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
        XphoneOs.onDisabled()
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
        NotificationStore()
        StampRallyStore()
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
        addCommand("xdebug", CommandXDebug())
        addCommand("stamp", CommandStamp())
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
        pm.registerEvents(NotificationHandler(), this)
        logger.info("Loaded NotificationHandler")
        pm.registerEvents(StampRallyHandler(), this)
        logger.info("Loaded StampRallyHandler")
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
    private val random = Random()

    companion object {
        @JvmStatic
        lateinit var instance: XCorePlugin
            private set
    }
}