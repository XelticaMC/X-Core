package work.xeltica.craft.core.modules.payments

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.plugins.VaultPlugin
import java.util.logging.Logger

object PaymentsModule : ModuleBase() {
    override fun onEnable() {
        logger = Bukkit.getLogger()
    }

    fun pay(from: Player, to: OfflinePlayer, amount: Int) {
        if (isEconomyDisabled) return
        economy.withdrawPlayer(from, amount.toDouble())
        economy.depositPlayer(to, amount.toDouble())

        from.sendMessage("${ChatColor.GREEN}${to.name}さんに${amount}${economy.currencyNameSingular()}を支払いました。")
        from.playSound(from.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 2f)
        val toPlayer = to.player
        if (toPlayer != null) {
            toPlayer.playSound(from.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 2f)
            toPlayer.sendMessage("${ChatColor.GREEN}${to.name}さんから${amount}${economy.currencyNameSingular()}を受け取りました。")
        }

        logger.info("${ChatColor.GREEN}${from.name}→${to.name}: ${amount}${economy.currencyNameSingular()}の支払い")
    }

    private val isEconomyDisabled get() = !VaultPlugin.getInstance().isEconomyEnabled
    private val economy get() = VaultPlugin.getInstance().economy

    private lateinit var logger: Logger
}