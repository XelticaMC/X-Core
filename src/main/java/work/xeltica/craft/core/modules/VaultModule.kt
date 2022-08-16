package work.xeltica.craft.core.modules

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.logging.Logger

/**
 * Vault プラグインと連携するためのX-Core プラグインです。
 * @author Xeltica
 */
object VaultModule : ModuleBase() {
    private const val VAULT_NOT_FOUND_ERROR_MESSAGE = "Vault is not found, so economy feature has been disabled.\""
    private const val ECONOMY_NOT_FOUND_ERROR_MESSAGE = "Economy plugin is not found, so economy feature has been disabled."
    override fun onEnable() {
        isEconomyEnabled = false
        logger = Bukkit.getLogger()
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.warning(VAULT_NOT_FOUND_ERROR_MESSAGE)
        } else {
            val registration = Bukkit.getServicesManager().getRegistration(Economy::class.java)
            if (registration == null) {
                logger.warning(ECONOMY_NOT_FOUND_ERROR_MESSAGE)
                return
            }
            economy = registration.provider
            isEconomyEnabled = true
            logger.info("Economy feature has been enabled!")
        }
    }

    override fun onDisable() {
        isEconomyEnabled = false
    }

    @JvmStatic
    fun tryDepositPlayer(p: Player?, amount: Double): Boolean {
        return economy!!.depositPlayer(p, amount).type == EconomyResponse.ResponseType.SUCCESS
    }

    @JvmStatic
    fun tryWithdrawPlayer(p: Player?, amount: Double): Boolean {
        return if (getBalance(p) - amount < 0) {
            false
        } else economy!!.withdrawPlayer(p, amount).type == EconomyResponse.ResponseType.SUCCESS
    }

    @JvmStatic
    fun getBalance(p: Player?): Double {
        return economy!!.getBalance(p)
    }

    @JvmStatic
    var isEconomyEnabled = false

    @JvmStatic
    var economy: Economy? = null
        private set

    private lateinit var logger: Logger
}