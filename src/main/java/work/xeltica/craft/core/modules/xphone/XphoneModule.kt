package work.xeltica.craft.core.modules.xphone

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.gui.SoundPitch
import work.xeltica.craft.core.hooks.FloodgateHook.isFloodgatePlayer
import work.xeltica.craft.core.modules.bedrock.BedrockToolsApp
import work.xeltica.craft.core.modules.cat.CatApp
import work.xeltica.craft.core.modules.ebipowerShop.EbiPowerDrugStoreApp
import work.xeltica.craft.core.modules.ebipowerShop.EbipowerStoreApp
import work.xeltica.craft.core.modules.eventHalloween.CandyStoreApp
import work.xeltica.craft.core.modules.eventSummer.EventCancelApp
import work.xeltica.craft.core.modules.eventSummer.EventRespawnApp
import work.xeltica.craft.core.modules.eventSummer.EventReturnWorldApp
import work.xeltica.craft.core.modules.eventSummer.FireworkApp
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintApp
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.livemode.LiveModeApp
import work.xeltica.craft.core.modules.notification.NotificationApp
import work.xeltica.craft.core.modules.omikuji.OmikujiApp
import work.xeltica.craft.core.modules.payments.PaymentsApp
import work.xeltica.craft.core.modules.promotion.PromoApp
import work.xeltica.craft.core.modules.punishment.PunishApp
import work.xeltica.craft.core.modules.quickchat.QuickChatApp
import work.xeltica.craft.core.modules.stamprally.StampRallyApp
import work.xeltica.craft.core.modules.transferGuide.TransferGuideApp
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataApp
import work.xeltica.craft.core.modules.vehicle.BoatApp
import work.xeltica.craft.core.modules.vehicle.CartApp
import work.xeltica.craft.core.modules.world.TeleportApp

/**
 * X Phone ゲームメニュー機能を提供します。
 */
object XphoneModule : ModuleBase() {
    const val ITEM_NAME_XPHONE = "xphone"
    const val PS_KEY_GIVEN_PHONE = "given_phone"

    private val PHONE_TITLE = "${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.ITALIC}ゲームメニュー"
    private lateinit var apps: MutableList<AppBase>

    /**
     * X-Core が有効になったときに呼ばれます。
     */
    override fun onEnable() {
        apps = mutableListOf(
            EventRespawnApp(),
            EventReturnWorldApp(),
            EventCancelApp(),
            TeleportApp(),
            NotificationApp(),
            ProtectApp(),
            BedrockToolsApp(),
            PromoApp(),
            SidebarApp(),
            OmikujiApp(),
            CatApp(),
            BoatApp(),
            CartApp(),
            EbipowerStoreApp(),
            EbiPowerDrugStoreApp(),
            FireworkApp(),
            HintApp(),
            LiveModeApp(),
            QuickChatApp(),
            TransferPlayerDataApp(),
            VoteApp(),
            PaymentsApp(),
            PunishApp(),
            StampRallyApp(),
            CandyStoreApp(),
            TransferGuideApp(),
        )

        registerCommand("xphone", XphoneCommand())
        registerHandler(XphoneHandler())
    }

    /**
     * X-Core が無効になったときに呼ばれます。
     */
    override fun onDisable() {
        apps.clear()
    }

    override fun onPostEnable() {
        ItemModule.registerItem(ITEM_NAME_XPHONE, ItemModule.createCustomItem("X Phone SE", "XelticaMCの独自機能にアクセスできるスマホ。"))
    }

    /**
     * X Phone にアプリを登録します。
     */
    fun registerApp(app: AppBase) {
        if (apps.contains(app)) {
            Bukkit.getLogger().warning("X Phoneアプリ「${app.javaClass.typeName}」は既に登録されているため、無視します。")
            return
        }
        apps.add(app)
        Bukkit.getLogger().warning("X Phoneアプリ「${app.javaClass.typeName}」を登録しました。")
    }

    /**
     * X Phone のメニュー画面を呼び出します。
     */
    fun openSpringBoard(player: Player) {
        playStartupSound(player)
        ui().openMenu(player, PHONE_TITLE, apps.filter {
            it.isVisible(player)
        }.map { app ->
            MenuItem(app.getName(player), { app.onLaunch(player) }, app.getIcon(player), null, app.isShiny(player))
        })
        HintModule.achieve(player, Hint.TWIN_XPHONE)
    }

    /**
     * プレイヤーが統合版であるかどうかを取得します。
     */
    @Deprecated("Use FloodgateHook instead.", ReplaceWith("player.isFloodgatePlayer()", "work.xeltica.craft.core.hooks.FloodgateHook.isFloodgatePlayer"))
    fun isBedrockPlayer(player: Player): Boolean {
        return player.isFloodgatePlayer()
    }

    fun ui() = Gui.getInstance()

    /**
     * 起動音を再生します。
     */
    fun playStartupSound(player: Player) {
        ui().playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.A1)
        ui().playSoundAfter(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.D2, 4)
        ui().playSoundAfter(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.C_2, 8)
    }

    /**
     * 通知音を再生します。
     */
    fun playTritone(player: Player) {
        ui().playSoundLocally(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.D1)
        ui().playSoundLocallyAfter(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.A1, 3)
        ui().playSoundLocallyAfter(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, SoundPitch.D2, 6)
    }
}