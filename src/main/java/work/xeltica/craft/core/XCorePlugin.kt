package work.xeltica.craft.core

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import work.xeltica.craft.core.api.HookBase
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.commands.CommandRegistry
import work.xeltica.craft.core.api.commands.CommandXDebug
import work.xeltica.craft.core.api.commands.CommandXReload
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.CitizensHook
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.hooks.FloodgateHook
import work.xeltica.craft.core.hooks.VaultHook
import work.xeltica.craft.core.modules.autoCrafter.AutoCrafterModule
import work.xeltica.craft.core.modules.bedrock.BedrockModule
import work.xeltica.craft.core.modules.bossbar.BossBarModule
import work.xeltica.craft.core.modules.cat.CatModule
import work.xeltica.craft.core.modules.clover.CloverModule
import work.xeltica.craft.core.modules.coreProtectGuiTest.CoreProtectGuiTestModule
import work.xeltica.craft.core.modules.countdown.CountdownModule
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.modules.ebipowerShop.EbiPowerShopModule
import work.xeltica.craft.core.modules.eventFarm.EventFarmModule
import work.xeltica.craft.core.modules.eventFirework.EventFireworkModule
import work.xeltica.craft.core.modules.eventHalloween.EventHalloweenModule
import work.xeltica.craft.core.modules.eventSummer.EventSummerModule
import work.xeltica.craft.core.modules.eventTwoYears.EventTwoYearsModule
import work.xeltica.craft.core.modules.fly.FlyModule
import work.xeltica.craft.core.modules.gamemodeChange.GamemodeChangeModule
import work.xeltica.craft.core.modules.grenade.GrenadeModule
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.kusa.KusaModule
import work.xeltica.craft.core.modules.livemode.LiveModeModule
import work.xeltica.craft.core.modules.meta.MetaModule
import work.xeltica.craft.core.modules.mobball.MobBallModule
import work.xeltica.craft.core.modules.moderation.ModerationModule
import work.xeltica.craft.core.modules.motd.MotdModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.modules.nightmare.NightmareModule
import work.xeltica.craft.core.modules.notification.NotificationModule
import work.xeltica.craft.core.modules.omikuji.OmikujiModule
import work.xeltica.craft.core.modules.payments.PaymentsModule
import work.xeltica.craft.core.modules.setMarker.SetMarkerModule
import work.xeltica.craft.core.modules.promotion.PromotionModule
import work.xeltica.craft.core.modules.punishment.PunishmentModule
import work.xeltica.craft.core.modules.quickchat.QuickChatModule
import work.xeltica.craft.core.modules.ranking.RankingModule
import work.xeltica.craft.core.modules.signEdit.SignEditModule
import work.xeltica.craft.core.modules.stamprally.StampRallyModule
import work.xeltica.craft.core.modules.transferGuide.TransferGuideModule
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataModule
import work.xeltica.craft.core.modules.vehicle.VehicleModule
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.modules.xphone.XphoneModule
import work.xeltica.craft.core.utils.Ticks

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Lutica
 */
class XCorePlugin : JavaPlugin() {

    override fun onEnable() {
        logger.info("${ChatColor.AQUA}------------------------------------------")
        logger.info("  ${ChatColor.GREEN}X-Core${ChatColor.GRAY} - XelticaMC Core System Plugin")
        logger.info("      ${ChatColor.GRAY}ver ${description.version}")
        logger.info("${ChatColor.AQUA}------------------------------------------")
        instance = this
        initializeFoundation()

        loadHooks()
        loadModules()
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

    /**
     * 連携フックを全て有効化します。
     */
    private fun loadHooks() {
        logger.info("連携フック 総数 ${hooks.size}")
        // 連携フックの有効化
        hooks.forEach {
            try {
                it.onEnable()
                logger.info("${ChatColor.GRAY}- ${it.javaClass.simpleName}${ChatColor.GREEN} » ○")
            } catch (e: Exception) {
                logger.severe("${ChatColor.GRAY}- ${it.javaClass.simpleName}${ChatColor.RED} » ×")
                e.printStackTrace()
            }
        }
        logger.info("連携フックを全て読み込み、有効化しました。")
    }

    /**
     * 連携フックを全て無効化します。
     */
    private fun unloadHooks() {
        // 連携フックの有効化
        hooks.forEach {
            try {
                it.onDisable()
            } catch (e: Exception) {
                logger.severe("連携フック '${it.javaClass.simpleName}' の無効化に失敗しました。")
                e.printStackTrace()
            }
        }
        logger.info("連携フックを全て無効化しました。")
    }

    /**
     * モジュールを全て有効化します。
     */
    private fun loadModules() {
        logger.info("モジュール 総数 ${modules.size}")
        // モジュールの有効化フック
        modules.forEach {
            try {
                it.onEnable()
                logger.info("${ChatColor.GRAY}- ${it.javaClass.simpleName}${ChatColor.GREEN} » ○")
            } catch (e: Exception) {
                logger.severe("${ChatColor.GRAY}- ${it.javaClass.simpleName}${ChatColor.RED} » ×")
                e.printStackTrace()
            }
        }
        // モジュールの有効化後処理フック（各モジュールの連携とか）
        modules.forEach {
            try {
                it.onPostEnable()
            } catch (e: Exception) {
                logger.severe("モジュール '${it.javaClass.simpleName}' の初期化後処理の実行に失敗しました。")
                e.printStackTrace()
            }
        }
        logger.info("モジュールを全て読み込み、有効化しました。")
    }

    /**
     * モジュールを全て無効化します。
     */
    private fun unloadModules() {
        // モジュールの無効化フック
        modules.forEach {
            try {
                it.onDisable()
            } catch (e: Exception) {
                logger.severe("モジュール '${it.javaClass.name}' の無効化に失敗しました。")
                e.printStackTrace()
            }
        }
        logger.info("モジュールを全て無効化しました。")
    }

    private fun initializeFoundation() {
        PlayerStore.onEnable()
        Gui.onEnable()
        CommandRegistry.register("xreload", CommandXReload())
        CommandRegistry.register("xdebug", CommandXDebug())
        TimeObserver().runTaskTimer(this, 0, Ticks.from(1.0).toLong())

        Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
    }

    private val hooks: Array<HookBase> = arrayOf(
        CitizensHook,
        DiscordHook,
        VaultHook,
        FloodgateHook,
    )

    private val modules: Array<ModuleBase> = arrayOf(
        AutoCrafterModule,
        BedrockModule,
        BossBarModule,
        CatModule,
        CloverModule,
        CountdownModule,
        CounterModule,
        EbiPowerModule,
        EbiPowerShopModule,
        EventFarmModule,
        EventFireworkModule,
        EventHalloweenModule,
        EventSummerModule,
        EventTwoYearsModule,
        FlyModule,
        GrenadeModule,
        HintModule,
        HubModule,
        ItemModule,
        KusaModule,
        LiveModeModule,
        MetaModule,
        MobBallModule,
        ModerationModule,
        MotdModule,
        NbsModule,
        NightmareModule,
        NotificationModule,
        OmikujiModule,
        PaymentsModule,
        PromotionModule,
        PunishmentModule,
        QuickChatModule,
        RankingModule,
        SignEditModule,
        StampRallyModule,
        TransferPlayerDataModule,
        VehicleModule,
        WorldModule,
        XphoneModule,
        TransferGuideModule,
        SetMarkerModule,
        CoreProtectGuiTestModule,
        GamemodeChangeModule,
    )

    companion object {
        lateinit var instance: XCorePlugin private set
    }
}