package work.xeltica.craft.core.hooks

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.HookBase

object VaultHook : HookBase() {
    private lateinit var economy: Economy
    override val isEnabled = Bukkit.getPluginManager().getPlugin("Vault") != null
    override fun onEnable() {
        val logger = Bukkit.getLogger()
        if (!isEnabled) {
            logger.warning("Vaultは検出されませんでした。経済機能は無効化されます。")
            return
        }
        val economy = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
        if (economy == null) {
            logger.warning("経済プラグインは検出されませんでした。経済機能は無効化されます。")
            return
        }
        this.economy = economy
    }

    fun tryDepositPlayer(player: Player, amount: Double): Boolean {
        if (!isEnabled) return false
        return economy.depositPlayer(player, amount).type == EconomyResponse.ResponseType.SUCCESS
    }

    fun tryWithdrawPlayer(player: Player, amount: Double): Boolean {
        return if (getBalance(player) - amount < 0) {
            false
        } else economy.withdrawPlayer(player, amount).type == EconomyResponse.ResponseType.SUCCESS
    }

    fun getBalance(p: OfflinePlayer): Double {
        return economy.getBalance(p)
    }

    val rawEconomyApi: Economy; get() = economy
}