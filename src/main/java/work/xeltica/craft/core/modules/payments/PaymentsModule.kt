package work.xeltica.craft.core.modules.payments

import org.bukkit.*
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.VaultHook
import java.util.logging.Logger

object PaymentsModule : ModuleBase() {
    override fun onEnable() {
        logger = Bukkit.getLogger()
    }

    fun pay(from: Player, to: OfflinePlayer, amount: Int) {
        if (!VaultHook.isEnabled) return
        val economy = VaultHook.rawEconomyApi
        economy.withdrawPlayer(from, amount.toDouble())
        economy.depositPlayer(to, amount.toDouble())
        val currencyName = economy.currencyNameSingular()

        from.sendMessage("${ChatColor.GREEN}${to.name}さんに${amount}${currencyName}を支払いました。")
        from.playSound(from.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 2f)

        val toPlayer = to.player
        if (toPlayer != null) {
            toPlayer.playSound(from.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 2f)
            toPlayer.sendMessage("${ChatColor.GREEN}${from.name}さんから${amount}${currencyName}を受け取りました。")
        }

        logger.info("${ChatColor.GREEN}${from.name}→${to.name}: ${amount}${currencyName}の支払い")
    }

    private lateinit var logger: Logger
}