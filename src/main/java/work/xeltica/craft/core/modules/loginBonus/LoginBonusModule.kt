package work.xeltica.craft.core.modules.loginBonus

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.utils.Ticks

/**
 * ログインボーナス機能を提供するモジュールです。
 */
object LoginBonusModule : ModuleBase() {
    const val PS_KEY_LOGIN_BONUS = "login_bonus"
    private const val LOGIN_BONUS = 250

    override fun onEnable() {
        registerHandler(LoginBonusHandler())
    }

    /**
     * [player] にログインボーナスを付与します。
     */
    fun giveLoginBonus(player: Player) {
        val record = PlayerStore.open(player)
        if (record.getBoolean(PS_KEY_LOGIN_BONUS)) return

        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (!player.isOnline) return@Runnable
            EbiPowerModule.tryGive(player, LOGIN_BONUS)
            player.sendMessage("§a§lログインボーナス達成！§6${LOGIN_BONUS}EP§fを手に入れた！")
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 2f)
            PlayerStore.open(player)[PS_KEY_LOGIN_BONUS] = true
        }, Ticks.from(2.0).toLong())
    }
}