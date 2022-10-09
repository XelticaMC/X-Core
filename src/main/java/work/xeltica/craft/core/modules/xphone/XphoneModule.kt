package work.xeltica.craft.core.modules.xphone

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.geysermc.floodgate.api.FloodgateApi
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.modules.halloween.CandyStoreApp
import work.xeltica.craft.core.modules.notification.NotificationApp
import work.xeltica.craft.core.modules.payments.PaymentsApp
import work.xeltica.craft.core.modules.quickchat.QuickChatApp
import work.xeltica.craft.core.stores.ItemStore
import work.xeltica.craft.core.xphone.apps.*
import java.lang.IllegalStateException

/**
 * X Phone の基幹となるシステムです。
 */
object XphoneModule : ModuleBase() {
    /**
     * X-Core が有効になったときに呼ばれます。
     */
    override fun onEnable() {
        apps.addAll(listOf(
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
            EbipowerDrugStoreApp(),
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
        ))

        registerCommand("xphone", XphoneCommand())
        registerHandler(XphoneHandler())
    }

    /**
     * X-Core が無効になったときに呼ばれます。
     */
    override fun onDisable() {
        apps.clear()
    }

    /**
     * X Phone にアプリを登録します。
     */
    fun registerApp(app: AppBase) {
        if (apps.contains(app)) {
            Bukkit.getLogger().warning("X Phoneアプリ「${app.javaClass.name}」は既に登録されているため、無視します。")
        }
        apps.add(app)
    }

    /**
     * X Phone のメニュー画面を呼び出します。
     */
    fun openSpringBoard(player: Player) {
        playStartupSound(player)
        ui().openMenu(player, name, apps.filter {
            it.isVisible(player)
        }.map { app ->
            MenuItem(app.getName(player), { app.onLaunch(player) }, app.getIcon(player), null, app.isShiny(player))
        })
    }

    /**
     * プレイヤーが統合版であるかどうかを取得します。
     */
    fun isBedrockPlayer(player: Player): Boolean {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.uniqueId)
    }

    fun ui() = Gui.getInstance() ?: throw IllegalStateException("Try to call ui() in X Phone OS, but X-Core is not fully initialized.")
    fun store() = ItemStore.getInstance() ?: throw IllegalStateException("Try to call store() in X Phone OS, but X-Core is not fully initialized.")

    /**
     * 起動音を再生します。
     */
    fun playStartupSound(player: Player) {
        ui().playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, SoundPitch.A1)
        ui().playSoundAfter(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, SoundPitch.D2, 4)
        ui().playSoundAfter(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, SoundPitch.C_2, 8)
    }

    /**
     * 通知音を再生します。
     */
    fun playTritone(player: Player) {
        ui().playSoundLocally(player, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1f, SoundPitch.D1)
        ui().playSoundLocallyAfter(player, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1f, SoundPitch.A1, 2)
        ui().playSoundLocallyAfter(player, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1f, SoundPitch.D2, 4)
    }

    const val name = "X Phone OS 3.0"

    private val apps = mutableListOf<AppBase>()
}