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

object LoginBonusModule : ModuleBase() {
    const val isReceivedLoginBonus = "login_bonus"
    const val isReceivedSummerLoginBonus = "login_bonus_summer"
    const val loginBonusValue = 250

    override fun onEnable() {
        registerHandler(LoginBonusHandler())
    }

    fun giveLoginBonus(p: Player) {
        val record = PlayerStore.open(p)
        if (record.getBoolean(isReceivedLoginBonus)) return

        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (!p.isOnline) return@Runnable
            EbiPowerModule.tryGive(p, loginBonusValue)
            p.sendMessage("§a§lログインボーナス達成！§6${loginBonusValue}EP§fを手に入れた！")
            p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 2f)
            PlayerStore.open(p)[isReceivedLoginBonus] = true
        }, Ticks.from(2.0).toLong())
    }
}