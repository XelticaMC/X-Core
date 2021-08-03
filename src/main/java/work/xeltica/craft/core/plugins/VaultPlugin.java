package work.xeltica.craft.core.plugins;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import work.xeltica.craft.core.XCorePlugin;

/**
 * Vault プラグインと連携するためのX-Core プラグインです。
 * @author Xeltica
 */
public class VaultPlugin extends PluginBase {
    public static VaultPlugin getInstance() {
        return instance == null ? (instance = new VaultPlugin()) : instance;
    }

    @Override
    public void onEnable(XCorePlugin plugin) {
        setEconomyEnabled(false);

        this.logger = Bukkit.getLogger();
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.logger.warning("Vault is not found, so economy feature has been disabled.");
        } else {
            final var provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            final var econ = provider.getProvider();
            if (econ == null) {
                this.logger.warning("Economy plugin is not found, so economy feature has been disabled.");
            } else {
                this.setEconomyEnabled(true);
                this.logger.info("Economy feature has been enabled!");
                this.economy = econ;
            }
        }
        super.onEnable(plugin);
    }

    @Override
    public void onDisable(XCorePlugin plugin) {
        super.onDisable(plugin);
        setEconomyEnabled(false);
    }

    public boolean isEconomyEnabled() {
        return isEconomyEnabled;
    }

    public void setEconomyEnabled(boolean isEconomyEnabled) {
        this.isEconomyEnabled = isEconomyEnabled;
    }

    public boolean tryDepositPlayer(Player p, double amount) {
        return economy.depositPlayer(p, amount).type == ResponseType.SUCCESS;
    }

    public boolean tryWithdrawPlayer(Player p, double amount) {
        if (getBalance(p) - amount < 0) {
            return false;
        }
        return economy.withdrawPlayer(p, amount).type == ResponseType.SUCCESS;
    }

    public double getBalance(Player p) {
        return economy.getBalance(p);
    }

    public Economy getEconomy() {
        return economy;
    }

    private boolean isEconomyEnabled;
    private Economy economy;
    private Logger logger;

    private static VaultPlugin instance;
}
