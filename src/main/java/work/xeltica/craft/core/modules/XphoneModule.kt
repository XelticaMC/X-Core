package work.xeltica.craft.core.modules

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi
import work.xeltica.craft.core.models.MenuItem
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.xphoneApps.*
import java.lang.IllegalStateException

/**
 * X Phone の基幹となるシステムです。
 */
object XphoneModule : ModuleBase() {
    override fun onEnable() {
        registerApps(
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
            NotificationApp(),
            PunishApp(),
            StampRallyApp(),
        )
    }

    override fun onDisable() {
        apps.clear()
        super.onDisable()
    }

    /**
     * X Phone のメニュー画面を呼び出します。
     */
    fun openSpringBoard(player: Player) {
        playStartupSound(player)
        ui().openMenu(player, name, apps.filter {
            it.isVisible(player)
        }.map { app ->
            MenuItem(
                app.getName(player),
                { app.onLaunch(player) },
                app.getIcon(player),
                null,
                app.isShiny(player)
            )
        })
    }

    /**
     * プレイヤーが統合版であるかどうかを取得します。
     */
    fun isBedrockPlayer(player: Player): Boolean {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.uniqueId)
    }

    /**
     * X Phone アプリを登録します。
     */
    fun registerApps(vararg app: AppBase) {
        apps.addAll(listOf(*app))
    }

    fun ui() = UIModule.getInstance() ?: throw IllegalStateException("Try to call ui() in X Phone OS, but X-Core is not fully initialized.")

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

    const val name = "X Phone OS 2.2"

    private val apps = mutableListOf<AppBase>()
}